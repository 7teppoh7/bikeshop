package ru.labs.offerservice.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.labs.offerservice.entities.Characteristic;

public interface CharacteristicRepository extends CrudRepository<Characteristic, Integer> {
}
