package ru.labs.offerservice.entities;

import lombok.*;
import ru.labs.offerservice.entities.dto.PaidType;

import javax.persistence.*;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "offers")
public class Offer {

    private Integer id;

    private String name;

    private Float price;

    private Integer paidTypeId;

    @Transient
    private PaidType paidType;

    @ManyToOne(fetch = FetchType.EAGER)
    private Category category;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Characteristic> characteristics;
}
