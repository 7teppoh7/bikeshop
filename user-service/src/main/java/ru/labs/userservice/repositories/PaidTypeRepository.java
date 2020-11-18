package ru.labs.userservice.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.labs.userservice.entities.PaidType;

public interface PaidTypeRepository extends CrudRepository<PaidType, Integer> {

    PaidType findByName(String name);
}
