package ru.labs.userservice.services;

import org.springframework.stereotype.Service;
import ru.labs.userservice.entities.Address;
import ru.labs.userservice.entities.Customer;
import ru.labs.userservice.repositories.CustomerRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer findCustomerById(Integer id) {
        return customerRepository.findById(id).orElse(null);
    }

    public Customer findCustomerByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    public Iterable<Customer> findAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public void deleteCustomerById(Integer customerId) {
        customerRepository.deleteById(customerId);
    }


    public boolean validateCustomer(Customer customer) {
        return ((customer.getFirstName() != null) &&
                (customer.getLastName() != null) &&
                (customer.getEmail() != null) &&
                (customer.getPassword() != null) &&
                (customer.getPhoneNumber() != null) &&
                (validateAddress(customer.getAddress())));
    }

    public boolean isEmailPresent(String email) {
        return (customerRepository.findByEmail(email) != null);
    }

    public boolean isPhonePresent(String phone) {
        return (customerRepository.findByPhoneNumber(phone) != null);
    }

    public boolean validateAddress(Address address) {
        return ((address != null) &&
                (address.getCity() != null) &&
                (address.getCountry() != null) &&
                (address.getState() != null));
    }

    public Customer findByEmailAndPassword(String email, String password) {
        return customerRepository.findByEmailAndPassword(email, password);
    }
}
