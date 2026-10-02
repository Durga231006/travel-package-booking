package com.travel.booking.controller;

import com.travel.booking.dto.CostResponse;
import com.travel.booking.dto.PackageBookingStats;
import com.travel.booking.entity.TravelPackage;
import com.travel.booking.service.PackageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packages")
public class PackageController {

    private final PackageService packageService;

    public PackageController(PackageService packageService) {
        this.packageService = packageService;
    }

    @GetMapping
    public List<TravelPackage> getAll() {
        return packageService.findAll();
    }

    /** Subquery report: packages with above-average bookings. */
    @GetMapping("/above-average-bookings")
    public List<PackageBookingStats> getAboveAverageBookings() {
        return packageService.findPackagesAboveAverageBookings();
    }

    @GetMapping("/{id}")
    public TravelPackage getById(@PathVariable Long id) {
        return packageService.findById(id);
    }

    /** SQL function: GET /api/packages/1/cost?travelers=3 */
    @GetMapping("/{id}/cost")
    public CostResponse calculateCost(@PathVariable Long id, @RequestParam(defaultValue = "1") int travelers) {
        return packageService.calculateCost(id, travelers);
    }

    @PostMapping
    public ResponseEntity<TravelPackage> create(@Valid @RequestBody TravelPackage travelPackage) {
        return ResponseEntity.status(HttpStatus.CREATED).body(packageService.create(travelPackage));
    }

    @PutMapping("/{id}")
    public TravelPackage update(@PathVariable Long id, @Valid @RequestBody TravelPackage travelPackage) {
        return packageService.update(id, travelPackage);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        packageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
