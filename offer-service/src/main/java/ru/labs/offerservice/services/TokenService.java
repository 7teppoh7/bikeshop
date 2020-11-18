package ru.labs.offerservice.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.labs.offerservice.entities.dto.PaidType;

import java.util.Arrays;
import java.util.Set;

@Service
public class TokenService {

    @Value("${user-service.url}")
    private String USER_SERVICE_URL;

    public final static String USER_ROLE = "USER";
    public final static String MODER_ROLE = "SALES_MANAGER";
    public final static String ADMIN_ROLE = "ADMIN";

    private final RestTemplate restTemplate;

    public TokenService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean hasAnyRole(String token) {
        return isUser(token) || isSalesManager(token) || isAdmin(token);
    }

    public boolean isUser(String token) {
        return Arrays.asList(getRolesByToken(token)).contains(USER_ROLE);
    }

    public boolean isSalesManager(String token) {
        return Arrays.asList(getRolesByToken(token)).contains(MODER_ROLE);
    }

    public boolean isAdmin(String token) {
        return Arrays.asList(getRolesByToken(token)).contains(ADMIN_ROLE);
    }

    private String[] getRolesByToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, token);
        HttpEntity<Token> entity = new HttpEntity<>(new Token(token), headers);
        ResponseEntity<String[]> response = restTemplate.exchange(USER_SERVICE_URL + "/rolesByToken",
                HttpMethod.POST, entity, String[].class);
        return response.getBody();
    }

    public PaidType[] getPaidTypesByToken(String token){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, token);
        HttpEntity<Token> entity = new HttpEntity<>(new Token(token), headers);
        ResponseEntity<PaidType[]> response = restTemplate.exchange(USER_SERVICE_URL + "/paidTypesByToken",
                HttpMethod.POST, entity, PaidType[].class);
        return response.getBody();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Token {
        private String token;
    }
}
