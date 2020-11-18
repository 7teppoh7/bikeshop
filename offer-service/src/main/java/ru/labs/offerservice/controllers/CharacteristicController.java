package ru.labs.offerservice.controllers;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.labs.offerservice.entities.Characteristic;
import ru.labs.offerservice.services.CharacteristicService;
import ru.labs.offerservice.services.TokenService;

@RestController
@RequestMapping("/characteristic")
public class CharacteristicController {

    private final TokenService tokenService;
    private final CharacteristicService characteristicService;

    public CharacteristicController(TokenService tokenService, CharacteristicService characteristicService) {
        this.tokenService = tokenService;
        this.characteristicService = characteristicService;
    }

    @GetMapping("/{chara}")
    public Characteristic getCharacteristic(@RequestHeader(value = "Authorization", required = false) final String token, @PathVariable Characteristic chara) {
        if (token == null || !tokenService.hasAnyRole(token))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This method is only for authorized users");

        if (chara == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Such characteristic doesn't exist");

        return characteristicService.findCharacteristicById(chara.getId());
    }

    @GetMapping("/all")
    public Iterable<Characteristic> getAllChars(@RequestHeader(value = "Authorization", required = false) final String token) {
        if (token == null || !tokenService.hasAnyRole(token))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This method is only for authorized users");

        return characteristicService.findAllCharacteristics();
    }

    @PostMapping("/create")
    public Characteristic createChara(@RequestHeader(value = "Authorization", required = false) final String token, @RequestBody Characteristic chara) {
        if (token == null || !(tokenService.isAdmin(token) || tokenService.isSalesManager(token)))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You have no authorities for this method");

        chara.setId(null); //create only
        if (!characteristicService.checkCharacteristic(chara))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Characteristic fields must not be null");
        return characteristicService.saveCharacteristic(chara);
    }

    @DeleteMapping("/delete")
    @ResponseStatus(value = HttpStatus.OK, reason = "Deleted successfully")
    public void deleteCharacteristic(@RequestHeader(value = "Authorization", required = false) final String token, @RequestParam Integer id) {
        if (token == null || !(tokenService.isAdmin(token) || tokenService.isSalesManager(token)))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You have no authorities for this method");

        if (characteristicService.findCharacteristicById(id) == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Such characteristic doesn't exist");
        try {
            characteristicService.deleteCharacteristic(id);
        } catch (DataAccessException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This characteristic is used");
        }
    }

    @PutMapping("/update")
    public Characteristic updateCustomerRef(@RequestHeader(value = "Authorization", required = false) final String token, @RequestBody Characteristic chara) {
        if (token == null || !(tokenService.isAdmin(token) || tokenService.isSalesManager(token)))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You have no authorities for this method");

        if (chara.getId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID must be present");
        if (!characteristicService.checkCharacteristic(chara))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Characteristic fields must not be null");
        return characteristicService.saveCharacteristic(chara);
    }
}
