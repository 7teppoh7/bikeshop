package ru.labs.orderservice.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    private Integer id;

    private String firstName;

    private String lastName;

    private String email; //TODO: uniq

    private String password;

    private String phoneNumber; //TODO: uniq

    private Address address;
}


