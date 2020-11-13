package ru.labs.offerservice.entities.dto;


import lombok.*;
import ru.labs.offerservice.entities.Offer;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private Integer id;

    private Offer offer;

    private CustomerDTO customer;

    private String name;

    private Date deliveryTime;

    private StatusDTO status;

    private Boolean paid;
}
