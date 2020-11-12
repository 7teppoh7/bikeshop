package ru.labs.offerservice.services;

import org.springframework.stereotype.Service;
import ru.labs.offerservice.entities.Characteristic;
import ru.labs.offerservice.repositories.CharacteristicRepository;

@Service
public class CharacteristicService {

    private final CharacteristicRepository characteristicRepository;

    public CharacteristicService(CharacteristicRepository characteristicRepository) {
        this.characteristicRepository = characteristicRepository;
    }

    public Characteristic findCharacteristicById(Integer charId){
        return characteristicRepository.findById(charId).orElse(null);
    }

    public Iterable<Characteristic> findAllCharacteristics(){
        return characteristicRepository.findAll();
    }

    public void deleteCharacteristic(Integer charId){
        characteristicRepository.deleteById(charId);
    }

    public Characteristic saveCharacteristic(Characteristic characteristic){
        return characteristicRepository.save(characteristic);
    }

    public Iterable<Characteristic> findAllById(Iterable<Integer> iterable) {
        return characteristicRepository.findAllById(iterable);
    }

    public boolean checkCharacteristic(Characteristic chara) {
        return (chara != null &&
                chara.getName() != null &&
                chara.getDescription() != null);
    }
}
