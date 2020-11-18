package ru.labs.offerservice.services;

import org.springframework.stereotype.Service;
import ru.labs.offerservice.entities.Characteristic;
import ru.labs.offerservice.entities.Offer;
import ru.labs.offerservice.repositories.OfferRepository;

import java.util.Set;

@Service
public class OfferService {

    private final OfferRepository offerRepository;

    public OfferService(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    public Offer findOfferById(Integer id){
        return offerRepository.findById(id).orElse(null);
    }

    public Iterable<Offer> findAllOffers(){
        return offerRepository.findAll();
    }

    public Offer saveOffer(Offer offer){
        return offerRepository.save(offer);
    }

    public void deleteOfferById(Integer offerId){
        offerRepository.deleteById(offerId);
    }

    public boolean validateOffer(Offer offer) { //TODO: @Valid
        return (offer.getPaidType() != null &&
                offer.getPaidType().getId() != null &&
                offer.getCategory() != null &&
                offer.getName() != null &&
                offer.getPrice() != null &&
                offer.getCategory().getId() != null &&
                validateCharacteristics(offer.getCharacteristics()));
    }

    private boolean validateCharacteristics(Set<Characteristic> characteristics) {
        return characteristics != null &&
                characteristics.stream().noneMatch((x) -> x.getId() != null);
    }

}
