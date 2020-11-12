package ru.labs.offerservice.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String name;

    private Float price;

    @JsonIgnore
    @ToString.Exclude
    private Integer paidTypeId;

    @Transient
    private PaidType paidType;

    @ManyToOne(fetch = FetchType.EAGER)
    private Category category;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Characteristic> characteristics;
}
