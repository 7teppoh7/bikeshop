package ru.labs.userservice.services;

import org.springframework.stereotype.Service;
import ru.labs.userservice.entities.Address;
import ru.labs.userservice.repositories.AddressRepository;

@Service
public class AddressService {

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public Address findByAddress(Address address){
        return findByCountryAndStateAndCity(address.getCountry(), address.getState(), address.getCity());
    }

    public Address findByCountryAndStateAndCity(String country, String state, String city){
        return addressRepository.findByCountryAndStateAndCity(country, state, city);
    }
}
