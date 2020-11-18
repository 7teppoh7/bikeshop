package ru.labs.offerservice.controllers;

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
import ru.labs.offerservice.entities.Category;
import ru.labs.offerservice.entities.Characteristic;
import ru.labs.offerservice.entities.Offer;
import ru.labs.offerservice.entities.dto.CustomerDTO;
import ru.labs.offerservice.entities.dto.Order;
import ru.labs.offerservice.entities.dto.PaidType;
import ru.labs.offerservice.entities.dto.StatusDTO;
import ru.labs.offerservice.services.CategoryService;
import ru.labs.offerservice.services.CharacteristicService;
import ru.labs.offerservice.services.OfferService;
import ru.labs.offerservice.services.TokenService;

import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/offer")
public class OfferController {

    @Value("${user-service.url}")
    private String USER_SERVICE_URL;

    @Value("${order-service.url}")
    private String ORDER_SERVICE_URL;

    private final RestTemplate restTemplate;
    private final TokenService tokenService;
    private final OfferService offerService;
    private final CharacteristicService characteristicService;
    private final CategoryService categoryService;

    public OfferController(RestTemplate restTemplate, TokenService tokenService, OfferService offerService, CharacteristicService characteristicService, CategoryService categoryService) {
        this.restTemplate = restTemplate;
        this.tokenService = tokenService;
        this.offerService = offerService;
        this.characteristicService = characteristicService;
        this.categoryService = categoryService;
    }

    @GetMapping("/{offer}")
    public Offer getOffer(@RequestHeader(value = "Authorization", required = false) final String token, @PathVariable Offer offer) {
        if (token == null || !tokenService.hasAnyRole(token))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This method is only for authorized users");

        if (offer == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found");

        if (offer.getPaidTypeId() != null) {
            try {
                HttpEntity entity = new HttpEntity(getHeaders(token));
                ResponseEntity<PaidType> response = restTemplate.exchange(USER_SERVICE_URL + "/paid-type/" + offer.getPaidTypeId(), HttpMethod.GET, entity, PaidType.class);
                offer.setPaidType(response.getBody());
            } catch (HttpClientErrorException.NotFound ex) {
                offer.setPaidTypeId(null); //paidType deleted or updated (??)
                offer.setPaidType(null);
                offerService.saveOffer(offer);
            }
        }

        return offer;
    }

    @GetMapping("/offers")
    public List<Offer> getAllOffers(@RequestHeader(value = "Authorization", required = false) final String token) {
        if (token == null || !tokenService.hasAnyRole(token))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This method is only for authorized users");

        List<Offer> result = new ArrayList<>();
        offerService.findAllOffers().forEach((x) -> result.add(getOffer(token, x)));
        return result;
    }

    @PostMapping("/create")
    @ResponseStatus(value = HttpStatus.CREATED, reason = "Created successfully")
    public Offer createOffer(@RequestHeader(value = "Authorization", required = false) final String token, @RequestBody Offer offer) {
        if (token == null || !(tokenService.isAdmin(token) || tokenService.isSalesManager(token)))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You have no authorities for this method");

        if (!offerService.validateOffer(offer))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Offer fields must not be null");

        offer.setId(null); //create only

        setRealValues(offer, token);

        return offerService.saveOffer(offer);
    }

    @DeleteMapping("/delete")
    @ResponseStatus(value = HttpStatus.OK, reason = "Deleted successfully")
    public void deleteOffer(@RequestHeader(value = "Authorization", required = false) final String token, @RequestParam Integer id) {
        if (token == null || !(tokenService.isAdmin(token) || tokenService.isSalesManager(token)))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You have no authorities for this method");

        if (offerService.findOfferById(id) == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Such offer doesn't exist");
        offerService.deleteOfferById(id);
    }

    @PutMapping("/update")
    @SneakyThrows
    public Offer updateCustomerRef(@RequestHeader(value = "Authorization", required = false) final String token, @RequestBody Offer offer) {
        if (token == null || !(tokenService.isAdmin(token) || tokenService.isSalesManager(token)))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You have no authorities for this method");

        if (offer.getId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID must be present");
        Offer offerToUpdate = offerService.findOfferById(offer.getId());
        if (offerToUpdate == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Such offer doesn't exist");

        setRealValues(offer, token);

        Map<String, Field> fieldMap = Arrays.stream(offer.getClass().getDeclaredFields())
                .filter((x) -> !x.isAnnotationPresent(JsonIgnore.class) || !x.isAnnotationPresent(Transient.class))
                .collect(Collectors.toMap((x) -> {
                    String fieldName = x.getName();
                    return fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1); //name -> Name
                }, Function.identity()));

        for (Method met : offer.getClass().getDeclaredMethods()) {
            String possibleKey = met.getName().substring(3); //cut get
            if (met.getName().startsWith("get") && fieldMap.containsKey(possibleKey)) {
                Field field = fieldMap.get(possibleKey);
                Method setMet = offer.getClass().getMethod("set" + possibleKey, field.getType());
                Object result = met.invoke(offer);
                if (result != null) setMet.invoke(offerToUpdate, result);
            }
        }
        return getOffer(token, offerService.saveOffer(offerToUpdate)); //to put real paidType in return object
    }

    @GetMapping("/availableOffers")
    public Set<Offer> getAvailableOffersByUserPaidTypes(@RequestHeader(value = "Authorization", required = false) final String token) {
        if (token == null || !tokenService.hasAnyRole(token))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This method is only for authorized users");

        List<PaidType> userPaidTypes = Arrays.asList(tokenService.getPaidTypesByToken(token));
        return getAllOffers(token).stream().filter((x) -> userPaidTypes.contains(x.getPaidType())).collect(Collectors.toSet());
    }

    @PostMapping("/{offer}/buy")
    @ResponseStatus(value = HttpStatus.OK, reason = "Order is created")
    public void buyOffer(@RequestHeader(value = "Authorization", required = false) final String token, @PathVariable Offer offer) {
        if (token == null || !tokenService.hasAnyRole(token))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This method is only for authorized users");

        if (offer == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found");

        HttpEntity<Token> entity = new HttpEntity<>(new Token(token), getHeaders(token));
        ResponseEntity<IdResponse> response = restTemplate.exchange(USER_SERVICE_URL + "/customerIdByToken/", HttpMethod.POST, entity, IdResponse.class);
        Integer customerId = response.getBody().id;

        Order order = new Order();
        order.setCustomer(new CustomerDTO(customerId));
        order.setName("Заказ пользователя " + customerId + " на предложение " + offer.getId());
        order.setOffer(offer);
        order.setStatus(new StatusDTO(1)); //TODO: get status from order service?
        order.setPaid(false);
        order.setDeliveryTime(new Date());
        HttpEntity<Order> buyRequest = new HttpEntity<>(order, getHeaders(token));
        ResponseEntity<Order> buyResponse = restTemplate.exchange(ORDER_SERVICE_URL + "/order/create", HttpMethod.POST, buyRequest, Order.class);
    }


    private void setRealValues(Offer offer, String token) {
        if (offer.getPaidType() != null && offer.getPaidType().getId() != null) {
            //check that paidType exists
            HttpEntity entity = new HttpEntity(getHeaders(token));
            ResponseEntity<PaidType> response = restTemplate.exchange(USER_SERVICE_URL + "/paid-type/" + offer.getPaidType().getId(), HttpMethod.GET, entity, PaidType.class);
            //if paidType exist... (else 404)
            offer.setPaidType(response.getBody());
            offer.setPaidTypeId(offer.getPaidType().getId());
        }

        if (offer.getCategory() != null && offer.getCategory().getId() != null) {
            //check that category with such id exists
            Category category = categoryService.findCategoryById(offer.getCategory().getId());
            if (category == null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Such category doesn't exist");
            offer.setCategory(category);
        }

        if (offer.getCharacteristics() != null) {
            //check that characteristics with such ids exist
            Set<Characteristic> chars = StreamSupport.stream( //TODO: test chars
                    characteristicService.findAllById(offer.getCharacteristics().stream()
                            .filter(Objects::nonNull)
                            .map(Characteristic::getId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet()))
                            .spliterator(), false).collect(Collectors.toSet());
            offer.setCharacteristics(chars);
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
