package ru.labs.userservice.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.labs.userservice.entities.Customer;

@RestController
public class UserController {

    @GetMapping("/test")
    public Customer test() {
        return new Customer(1, "Petya", "Petrov", "pertov@mail.ru", "SECRET", "555000", null);
    }
}
