package ru.labs.userservice.entities;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String firstName;

    private String lastName;

    private String email; //TODO: uniq

    private String password;

    private String phoneNumber; //TODO: uniq

    @ManyToOne(fetch = FetchType.EAGER)
    private Address address;

    @ManyToMany(fetch = FetchType.EAGER)
    @EqualsAndHashCode.Exclude
    private Set<PaidType> paidTypeSet;
}

