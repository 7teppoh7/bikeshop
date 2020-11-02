package ru.labs.orderservice.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Offer {

    private Integer id;

    private String name;

    private Float price;

    private PaidType paidType;

    private Category category;
}
