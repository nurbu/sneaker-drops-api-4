package com.pluralsight.sneakerdropsapi.service;

import com.pluralsight.sneakerdropsapi.data.BrandRepository;
import com.pluralsight.sneakerdropsapi.data.SneakerRepository;
import com.pluralsight.sneakerdropsapi.models.Brand;
import com.pluralsight.sneakerdropsapi.models.Sneaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class SneakerService {

    private final SneakerRepository sneakerRepository;
    private final BrandRepository brandRepository;

    @Autowired
    public SneakerService(SneakerRepository sneakerRepository, BrandRepository brandRepository) {
        this.sneakerRepository = sneakerRepository;
        this.brandRepository = brandRepository;
    }

    public Sneaker findById(Long id) {
        return sneakerRepository.findById(id).orElse(null);
    }

    public List<Sneaker> search(Integer year, String model, String brand,
                                Double minPrice, Double maxPrice) {
        Stream<Sneaker> stream = sneakerRepository.findAll().stream();

        if (year != null) {
            stream = stream.filter(s -> s.getReleaseYear() == year);
        }
        if (model != null && !model.isBlank()) {
            stream = stream.filter(s -> s.getModel().toLowerCase()
                    .contains(model.toLowerCase()));
        }
        if (brand != null && !brand.isBlank()) {
            stream = stream.filter(s -> s.getBrand().getName().equalsIgnoreCase(brand));
        }
        if (minPrice != null) {
            stream = stream.filter(s -> s.getPrice() >= minPrice);
        }
        if (maxPrice != null) {
            stream = stream.filter(s -> s.getPrice() <= maxPrice);
        }

        return stream.toList();
    }

    public Sneaker add(Sneaker sneaker) {
        Long brandId = sneaker.getBrand().getId();
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new RuntimeException("No brand found with id " + brandId));
        sneaker.setBrand(brand);
        return sneakerRepository.save(sneaker);
    }

    public Sneaker update(Long id, Sneaker updatedSneaker) {
        Sneaker existing = findById(id);
        if (existing == null) {
            return null;
        }

        existing.setModel(updatedSneaker.getModel());
        existing.setPrice(updatedSneaker.getPrice());
        existing.setReleaseYear(updatedSneaker.getReleaseYear());

        if (updatedSneaker.getBrand() != null && updatedSneaker.getBrand().getId() != null) {
            Long brandId = updatedSneaker.getBrand().getId();
            Brand brand = brandRepository.findById(brandId)
                    .orElseThrow(() -> new RuntimeException("No brand found with id " + brandId));
            existing.setBrand(brand);
        }

        return sneakerRepository.save(existing);
    }


    public Sneaker updatePrice(Long id, double newPrice) {
        Sneaker sneaker = findById(id);
        if (sneaker == null) throw new NotFoundException(id);
        sneaker.setPrice(newPrice);
        return sneakerRepository.save(sneaker);
    }

    public void delete(Long id) {
        if (!sneakerRepository.existsById(id)) {
            throw new NotFoundException(id);
        }
        sneakerRepository.deleteById(id);
    }
}