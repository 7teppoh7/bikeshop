package ru.labs.orderservice.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    private Integer id;

    private String city;

    private String state;

    private String country;
}
