package com.travel.booking.service;

import com.travel.booking.entity.Destination;
import com.travel.booking.exception.ResourceNotFoundException;
import com.travel.booking.repository.DestinationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DestinationService {

    private final DestinationRepository destinationRepository;

    public DestinationService(DestinationRepository destinationRepository) {
        this.destinationRepository = destinationRepository;
    }

    public List<Destination> findAll() {
        return destinationRepository.findAll();
    }

    public Destination findById(Long id) {
        return destinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with id " + id));
    }

    public Destination create(Destination destination) {
        destination.setDestinationId(null);
        return destinationRepository.save(destination);
    }

    public Destination update(Long id, Destination updated) {
        Destination existing = findById(id);
        existing.setName(updated.getName());
        existing.setCountry(updated.getCountry());
        existing.setDescription(updated.getDescription());
        return destinationRepository.save(existing);
    }

    public void delete(Long id) {
        destinationRepository.delete(findById(id));
    }
}
