package ru.labs.orderservice.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import ru.labs.orderservice.entity.dto.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @JsonIgnore
    @ToString.Exclude
    private Integer offerId;

    @Transient
    private Offer offer;

    @JsonIgnore
    @ToString.Exclude
    private Integer customerId;

    @Transient
    private Customer customer;

    private String name;

    private Date deliveryTime;

    @ManyToOne(fetch = FetchType.EAGER)
    private Status status;

    private Boolean paid;
}
