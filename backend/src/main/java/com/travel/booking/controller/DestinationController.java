package com.travel.booking.controller;

import com.travel.booking.entity.Destination;
import com.travel.booking.service.DestinationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/destinations")
public class DestinationController {

    private final DestinationService destinationService;

    public DestinationController(DestinationService destinationService) {
        this.destinationService = destinationService;
    }

    @GetMapping
    public List<Destination> getAll() {
        return destinationService.findAll();
    }

    @GetMapping("/{id}")
    public Destination getById(@PathVariable Long id) {
        return destinationService.findById(id);
    }

    @PostMapping
    public ResponseEntity<Destination> create(@Valid @RequestBody Destination destination) {
        return ResponseEntity.status(HttpStatus.CREATED).body(destinationService.create(destination));
    }

    @PutMapping("/{id}")
    public Destination update(@PathVariable Long id, @Valid @RequestBody Destination destination) {
        return destinationService.update(id, destination);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        destinationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
