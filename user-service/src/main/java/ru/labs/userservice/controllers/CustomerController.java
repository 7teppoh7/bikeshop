package ru.labs.userservice.controllers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.labs.userservice.entities.Address;
import ru.labs.userservice.entities.Customer;
import ru.labs.userservice.entities.PaidType;
import ru.labs.userservice.entities.Role;
import ru.labs.userservice.services.AddressService;
import ru.labs.userservice.services.CustomerService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    private final CustomerService customerService;
    private final AddressService addressService;

    public CustomerController(CustomerService customerService, AddressService addressService) {
        this.customerService = customerService;
        this.addressService = addressService;
    }

    @GetMapping("/{customerId}")
    public Customer getCustomer(@PathVariable Integer customerId) {
        Customer customer = customerService.findCustomerById(customerId);
        if (customer == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Such customer doesn't exist");
        return customer;
    }

    @GetMapping("/{customerId}/paidtypes")
    public Set<PaidType> getCustomerPaidType(@PathVariable Integer customerId) {
        Customer customer = customerService.findCustomerById(customerId);
        if (customer == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Such customer doesn't exist");
        return customer.getPaidTypeSet();
    }

    @GetMapping("/customers")
    public Iterable<Customer> getAllCustomers() {
        return customerService.findAllCustomers();
    }

    @PostMapping("/create")
    public Customer createCustomer(@RequestBody Customer customer) {
        if (!customerService.validateCustomer(customer))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer fields should be NotNull");
        if (customerService.isEmailPresent(customer.getEmail()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already used");
        if (customerService.isPhonePresent(customer.getPhoneNumber()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone is already used");
        Address adrFromDb = addressService.findByAddress(customer.getAddress());
        if (adrFromDb != null) customer.setAddress(adrFromDb);
        customer.setId(null);
        customer.setCustomerRoles(Set.of(Role.USER));
        return customerService.saveCustomer(customer);
    }

    @DeleteMapping("/delete")
    @ResponseStatus(value = HttpStatus.OK, reason = "Deleted successfully")
    public void deleteCustomer(@RequestParam Integer id) {
        if (customerService.findCustomerById(id) == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Such customer doesn't exist");
        customerService.deleteCustomerById(id);
    }

    @PutMapping("/update")
    @SneakyThrows
    public Customer updateCustomerRef(@RequestBody Customer customer) {
        Customer updated = updateCheck(customer);

        Map<String, Field> fieldMap = Arrays.stream(customer.getClass().getDeclaredFields())
                .filter((x) -> !x.isAnnotationPresent(JsonIgnore.class))
                .collect(Collectors.toMap((x) -> {
                    String fieldName = x.getName();
                    return fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1); //password -> Password
                }, Function.identity()));

        for (Method met : customer.getClass().getDeclaredMethods()) {
            String possibleKey = met.getName().substring(3); //cut get
            if (met.getName().startsWith("get") && fieldMap.containsKey(possibleKey)){
                Field field = fieldMap.get(possibleKey);
                Method setMet = customer.getClass().getMethod("set" + possibleKey, field.getType());
                Object result = met.invoke(customer);
                if (result != null) setMet.invoke(updated, result);
            }
        }
        return updated;
    }

    private Customer updateCheck(Customer customer) {
        if (customer.getId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID must be present");
        Customer oldCustomer = customerService.findCustomerById(customer.getId());
        if (oldCustomer == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Such customer doesn't exist");
        String email = customer.getEmail();
        if (email != null && !email.equals(oldCustomer.getEmail()) && customerService.isEmailPresent(email)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already used");
        String phone = customer.getPhoneNumber();
        if (phone != null && !phone.equals(oldCustomer.getPhoneNumber()) && customerService.isPhonePresent(phone)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone is already used");
        return oldCustomer;
    }
}
