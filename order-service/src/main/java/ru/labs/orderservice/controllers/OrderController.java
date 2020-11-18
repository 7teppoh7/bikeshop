package ru.labs.orderservice.controllers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import ru.labs.orderservice.entity.Order;
import ru.labs.orderservice.entity.Status;
import ru.labs.orderservice.entity.dto.Customer;
import ru.labs.orderservice.entity.dto.Offer;
import ru.labs.orderservice.services.OrderService;
import ru.labs.orderservice.services.StatusService;
import ru.labs.orderservice.services.TokenService;

import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Value("${user-service.url}")
    private String USER_SERVICE_URL;

    @Value("${offer-service.url}")
    private String OFFER_SERVICE_URL;

    private final RestTemplate restTemplate;
    private final TokenService tokenService;
    private final OrderService orderService;
    private final StatusService statusService;

    public OrderController(RestTemplate restTemplate, TokenService tokenService, OrderService orderService, StatusService statusService) {
        this.restTemplate = restTemplate;
        this.tokenService = tokenService;
        this.orderService = orderService;
        this.statusService = statusService;
    }

    @GetMapping("/{order}")
    public Order getOrder(@RequestHeader(value = "Authorization", required = false) final String token, @PathVariable Order order) {
        if (token == null || !tokenService.hasAnyRole(token))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This method is only for authorized users");

        if (order == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");

        if (order.getOfferId() != null) {
            HttpEntity entity = new HttpEntity(getHeaders(token));
            ResponseEntity<Offer> response = restTemplate.exchange(OFFER_SERVICE_URL + "/offer/" + order.getOfferId(), HttpMethod.GET, entity, Offer.class);
            order.setOffer(response.getBody());
        }

        if (order.getCustomerId() != null) {
            HttpEntity entity = new HttpEntity(getHeaders(token));
            ResponseEntity<Customer> response = restTemplate.exchange(USER_SERVICE_URL + "/customer/" + order.getCustomerId(), HttpMethod.GET, entity, Customer.class);
            order.setCustomer(response.getBody());
        }

        return order;
    }

    @PostMapping("/synchronizeDB")
    public void synchronizeDB(@RequestHeader(value = "Authorization", required = false) final String token) {
        if (token == null || !(tokenService.isAdmin(token) || tokenService.isSalesManager(token)))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You have no authorities for this method");

        for (Order order : getAllOrders(token)) {
            if (order.getCustomerId() != null) {
                try {
                    HttpEntity entity = new HttpEntity(getHeaders(token));
                    ResponseEntity<Customer> response = restTemplate.exchange(USER_SERVICE_URL + "/customer/" + order.getCustomerId(), HttpMethod.GET, entity, Customer.class);
                    order.setCustomer(response.getBody());
                } catch (HttpClientErrorException.NotFound ex) {
                    order.setCustomerId(null); //customer deleted or updated (??)
                    order.setCustomer(null);
                    orderService.saveOrder(order);
                }
            }
            if (order.getOfferId() != null) {
                try {
                    HttpEntity entity = new HttpEntity(getHeaders(token));
                    ResponseEntity<Offer> response = restTemplate.exchange(OFFER_SERVICE_URL + "/offer/" + order.getOfferId(), HttpMethod.GET, entity, Offer.class);
                    order.setOffer(response.getBody());
                } catch (HttpClientErrorException.NotFound ex) {
                    order.setOfferId(null); //offer deleted or updated (??)
                    order.setOffer(null);
                    orderService.saveOrder(order);
                }
            }

        }
    }

    @GetMapping("/orders")
    public List<Order> getAllOrders(@RequestHeader(value = "Authorization", required = false) final String token) {
        if (token == null || !tokenService.hasAnyRole(token))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This method is only for authorized users");

        List<Order> result = new ArrayList<>();
        orderService.findAllOrders().forEach((x) -> result.add(getOrder(token, x)));
        return result;
    }

    @PostMapping("/create")
    @ResponseStatus(value = HttpStatus.CREATED, reason = "Created successfully")
    public Order createOrder(@RequestHeader(value = "Authorization", required = false) final String token, @RequestBody Order order) {
        if (token == null || !tokenService.hasAnyRole(token))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This method is only for authorized users");

        if (!orderService.validateOrder(order))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order fields must not be null");

        order.setId(null); //create only

        setRealValues(order, token);

        return orderService.saveOrder(order);
    }

    @DeleteMapping("/delete")
    @ResponseStatus(value = HttpStatus.OK, reason = "Deleted successfully")
    public void deleteOrder(@RequestHeader(value = "Authorization", required = false) final String token, @RequestParam Integer id) {
        if (token == null || !tokenService.hasAnyRole(token))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This method is only for authorized users");

        if (orderService.findOrderById(id) == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Such order doesn't exist");
        orderService.deleteOrderById(id);
    }

    @PutMapping("/update")
    @SneakyThrows
    public Order updateOrder(@RequestHeader(value = "Authorization", required = false) final String token, @RequestBody Order order) {
        if (token == null || !tokenService.hasAnyRole(token))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This method is only for authorized users");

        if (order.getId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID must be present");
        Order orderToUpdate = orderService.findOrderById(order.getId());
        if (orderToUpdate == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Such order doesn't exist");

        setRealValues(order, token);

        Map<String, Field> fieldMap = Arrays.stream(order.getClass().getDeclaredFields())
                .filter((x) -> !x.isAnnotationPresent(JsonIgnore.class) || !x.isAnnotationPresent(Transient.class))
                .collect(Collectors.toMap((x) -> {
                    String fieldName = x.getName();
                    return fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1); //name -> Name
                }, Function.identity()));

        for (Method met : order.getClass().getDeclaredMethods()) {
            String possibleKey = met.getName().substring(3); //cut get
            if (met.getName().startsWith("get") && fieldMap.containsKey(possibleKey)) {
                Field field = fieldMap.get(possibleKey);
                Method setMet = order.getClass().getMethod("set" + possibleKey, field.getType());
                Object result = met.invoke(order);
                if (result != null) setMet.invoke(orderToUpdate, result);
            }
        }
        return getOrder(token, orderService.saveOrder(orderToUpdate)); //to put real values in return object
    }

    @PatchMapping("/{order}")
    public Order updateStatus(@RequestHeader(value = "Authorization", required = false) final String token, @PathVariable Order order, @RequestBody Integer id) {
        if (token == null || !(tokenService.isAdmin(token) || tokenService.isSalesManager(token)))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You have no authorities for this method");

        if (order == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Such order doesn't exist");

        Status newStatus = statusService.findStatusById(id);
        if (newStatus == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Such status doesn't exist");

        order.setStatus(newStatus);
        return getOrder(token, orderService.saveOrder(order));
    }

    @PostMapping("/buy")
    @ResponseStatus(value = HttpStatus.CREATED, reason = "Order created")
    public Order buyOffer(@RequestHeader(value = "Authorization", required = false) final String token, @RequestBody Offer offer) {
        if (token == null || !tokenService.hasAnyRole(token))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This method is only for authorized users");

        if (offer == null || offer.getId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Offer cannot be null");

        HttpEntity<Token> entity = new HttpEntity<>(new Token(token), getHeaders(token));
        ResponseEntity<IdResponse> response = restTemplate.exchange(USER_SERVICE_URL + "/customerIdByToken/", HttpMethod.POST, entity, IdResponse.class);
        Integer customerId = response.getBody().id;

        Order order = orderService.createOrder(customerId, offer);
        return getOrder(token, order);
    }

    private void setRealValues(Order order, String token) {

        if (order.getStatus() != null && order.getStatus().getId() != null) {
            Status status = statusService.findStatusById(order.getStatus().getId());
            if (status == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Such status doesn't exist");
            order.setStatus(status);
        }

        if (order.getOffer() != null && order.getOffer().getId() != null) {
            //check that offer exists
            HttpEntity entity = new HttpEntity(getHeaders(token));
            ResponseEntity<Offer> response = restTemplate.exchange(OFFER_SERVICE_URL + "/offer/" + order.getOffer().getId(), HttpMethod.GET, entity, Offer.class);
            //if offer exists... (else 404)
            order.setOffer(response.getBody());
            order.setOfferId(order.getOffer().getId());
        }

        if (order.getCustomer() != null && order.getCustomer().getId() != null) {
            //check that customer exists
            HttpEntity entity = new HttpEntity(getHeaders(token));
            ResponseEntity<Customer> response = restTemplate.exchange(USER_SERVICE_URL + "/customer/" + order.getCustomer().getId(), HttpMethod.GET, entity, Customer.class);
            //if customer exists... (else 404)
            order.setCustomer(response.getBody());
            order.setCustomerId(order.getCustomer().getId());
        }
    }

    private HttpHeaders getHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, token);
        return headers;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class IdResponse {
        Integer id;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Token {
        String token;
    }

}
