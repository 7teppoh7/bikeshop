package ru.labs.orderservice.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.labs.orderservice.entity.Order;

public interface OrderRepository extends CrudRepository<Order,Integer> {
}
