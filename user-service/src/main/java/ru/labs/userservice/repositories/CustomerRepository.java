package ru.labs.userservice.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.labs.userservice.entities.Customer;

public interface CustomerRepository extends CrudRepository<Customer, Integer> {

    Customer findByEmail(String email);

    Customer findByPhoneNumber(String phone);

    Customer findByEmailAndPassword(String email, String password);
}
