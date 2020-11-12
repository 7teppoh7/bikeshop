package ru.labs.userservice.controllers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.labs.userservice.config.jwt.JwtTokenProvider;
import ru.labs.userservice.entities.Customer;
import ru.labs.userservice.entities.PaidType;
import ru.labs.userservice.services.CustomerService;

import java.util.List;
import java.util.Set;

@RestController
public class AuthController {

    private final JwtTokenProvider tokenProvider;
    private final CustomerService customerService;

    public AuthController(JwtTokenProvider tokenProvider, CustomerService customerService) {
        this.tokenProvider = tokenProvider;
        this.customerService = customerService;
    }

    @PostMapping("/login")
    public Token auth(@RequestBody AuthRequest request) {
        Customer customer = customerService.findByEmailAndPassword(request.getEmail(), request.getPassword());
        if (customer == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email or password are incorrect");
        String token = tokenProvider.createToken(customer.getEmail(), customer.getCustomerRoles());
        return new Token(token);
    }

    @PostMapping("/emailByToken")
    public EmailResponse getEmailByToken(@RequestBody Token token) {
        if (!tokenProvider.validateToken(token.getToken()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token");
        return new EmailResponse(tokenProvider.getEmailFromToken(token.getToken()));
    }

    @PostMapping("/rolesByToken")
    public List getRolesByToken(@RequestBody Token token) {
        if (!tokenProvider.validateToken(token.getToken()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token");
        return tokenProvider.getRolesFromToken(token.getToken());
    }

    @PostMapping("/paidTypesByToken")
    public Set<PaidType> getPaidTypesByToken(@RequestBody Token token) {
        if (!tokenProvider.validateToken(token.getToken()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token");
        return customerService.findCustomerByEmail(tokenProvider.getEmailFromToken(token.getToken())).getPaidTypeSet();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class EmailResponse {
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Token {
        private String token;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class AuthRequest {
        private String email;
        private String password;
    }

}
