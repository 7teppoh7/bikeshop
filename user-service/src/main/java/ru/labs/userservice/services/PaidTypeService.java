package ru.labs.userservice.services;

import org.springframework.stereotype.Service;
import ru.labs.userservice.entities.PaidType;
import ru.labs.userservice.repositories.PaidTypeRepository;

@Service
public class PaidTypeService {

    private final PaidTypeRepository paidTypeRepository;

    public PaidTypeService(PaidTypeRepository paidTypeRepository) {
        this.paidTypeRepository = paidTypeRepository;
    }

    public PaidType findPaidTypeById(Integer id){
        return paidTypeRepository.findById(id).orElse(null);
    }

    public Iterable<PaidType> findAllPaidTypes(){
        return paidTypeRepository.findAll();
    }

    public PaidType findPaidTypeByName(String name){ return paidTypeRepository.findByName(name);}

    public PaidType savePaidType(PaidType paidType){
        return paidTypeRepository.save(paidType);
    }

    public void deletePaidTypeById(Integer paidTypeId){
        paidTypeRepository.deleteById(paidTypeId);
    }
}
