package ru.labs.userservice.controllers;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.labs.userservice.entities.PaidType;
import ru.labs.userservice.services.PaidTypeService;

@RestController
@RequestMapping("/paidtype")
public class PaidTypeController {

    private final PaidTypeService paidTypeService;

    public PaidTypeController(PaidTypeService paidTypeService) {
        this.paidTypeService = paidTypeService;
    }

    @GetMapping("/{id}")
    public PaidType getPaidType(@PathVariable Integer id) {
        PaidType paidType = paidTypeService.findPaidTypeById(id);
        if (paidType == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Such Paid Type doesn't exist");
        return paidType;
    }

    @GetMapping("/paidtypes")
    public Iterable<PaidType> getAllPaidTypes() {
        return paidTypeService.findAllPaidTypes();
    }

    @PostMapping("/create")
    public PaidType savePaidType(@RequestBody PaidType paidType) {
        paidType.setId(null);
        if (paidType.getName() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        if (paidTypeService.findPaidTypeByName(paidType.getName()) != null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Such paid type is already exist");
        return paidTypeService.savePaidType(paidType);
    }

    @DeleteMapping("/delete")
    @ResponseStatus(value = HttpStatus.OK, reason = "Deleted successfully")
    public void deletePaidType(@RequestParam Integer id) {
        if (paidTypeService.findPaidTypeById(id) == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Such paid type doesn't exist");
        try {
            paidTypeService.deletePaidTypeById(id);
        } catch (DataAccessException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This paid type is used");
        }
    }

    @PutMapping("/update")
    public PaidType updateCustomer(@RequestBody PaidType newPaid) {
        if (newPaid.getId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID must be present");
        PaidType oldPaid = paidTypeService.findPaidTypeById(newPaid.getId());
        if (oldPaid == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This paid type isn't exist");
        if (paidTypeService.findPaidTypeByName(newPaid.getName()) != null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Such paid type name is already exist");
        oldPaid.setName(newPaid.getName());
        return paidTypeService.savePaidType(oldPaid);
    }
}
