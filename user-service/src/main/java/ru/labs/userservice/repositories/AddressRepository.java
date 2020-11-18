package ru.labs.userservice.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.labs.userservice.entities.Address;

public interface AddressRepository extends CrudRepository<Address, Integer> {

    Address findByCountryAndStateAndCity(String country, String state, String city);

}
