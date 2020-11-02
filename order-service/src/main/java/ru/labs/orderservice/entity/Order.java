package ru.labs.orderservice.entity;


import lombok.*;

import ru.labs.orderservice.entity.dto.*;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private Integer offerId;

    @Transient
    private Offer offer;

    private String name;

    private Date deliveryTime;

    @ManyToOne(fetch = FetchType.EAGER)
    private Status status;

    private Boolean paid;
}
