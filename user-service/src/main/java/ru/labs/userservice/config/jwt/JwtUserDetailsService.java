package ru.labs.userservice.config.jwt;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.labs.userservice.entities.Customer;
import ru.labs.userservice.services.CustomerService;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    private final CustomerService customerService;

    public JwtUserDetailsService(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Customer customer = customerService.findCustomerByEmail(email);
        if (customer == null) throw new UsernameNotFoundException("Customer with email " + email + " not found" );
        return customer;
    }
}
