package ru.labs.orderservice.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import ru.labs.orderservice.entity.dto.Customer;

@RestController
public class OrderController {

    private final RestTemplate restTemplate;

    public OrderController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/test")
    public Customer test() {
        return restTemplate.getForObject("http://localhost:8081/test", Customer.class); //TODO: change localhost to micro service name
    }
}
