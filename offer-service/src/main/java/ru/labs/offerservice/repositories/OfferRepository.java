package ru.labs.offerservice.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.labs.offerservice.entities.Offer;

public interface OfferRepository extends CrudRepository<Offer, Integer> {
}
