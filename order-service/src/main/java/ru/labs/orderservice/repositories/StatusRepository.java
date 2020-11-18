package ru.labs.orderservice.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.labs.orderservice.entity.Status;

public interface StatusRepository extends CrudRepository<Status, Integer> {
}
