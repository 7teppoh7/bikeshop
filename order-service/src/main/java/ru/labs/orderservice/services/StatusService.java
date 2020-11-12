package ru.labs.orderservice.services;

import org.springframework.stereotype.Service;
import ru.labs.orderservice.repositories.StatusRepository;

@Service
public class StatusService {

    private final StatusRepository statusRepository;

    public StatusService(StatusRepository statusRepository) {
        this.statusRepository = statusRepository;
    }
}
