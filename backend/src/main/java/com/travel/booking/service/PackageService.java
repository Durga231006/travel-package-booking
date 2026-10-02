package com.travel.booking.service;

import com.travel.booking.dto.CostResponse;
import com.travel.booking.dto.PackageBookingStats;
import com.travel.booking.entity.TravelPackage;
import com.travel.booking.exception.BookingException;
import com.travel.booking.exception.ResourceNotFoundException;
import com.travel.booking.repository.BookingJdbcRepository;
import com.travel.booking.repository.DestinationRepository;
import com.travel.booking.repository.TravelPackageRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PackageService {

    private final TravelPackageRepository packageRepository;
    private final DestinationRepository destinationRepository;
    private final BookingJdbcRepository bookingJdbcRepository;

    public PackageService(TravelPackageRepository packageRepository,
                          DestinationRepository destinationRepository,
                          BookingJdbcRepository bookingJdbcRepository) {
        this.packageRepository = packageRepository;
        this.destinationRepository = destinationRepository;
        this.bookingJdbcRepository = bookingJdbcRepository;
    }

    public List<TravelPackage> findAll() {
        return packageRepository.findAll();
    }

    public TravelPackage findById(Long id) {
        return packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found with id " + id));
    }

    public TravelPackage create(TravelPackage travelPackage) {
        checkDestinationExists(travelPackage.getDestinationId());
        travelPackage.setPackageId(null);
        // A new package starts with every seat free
        travelPackage.setAvailableSeats(travelPackage.getTotalSeats());
        travelPackage.setStatus("AVAILABLE");
        return packageRepository.save(travelPackage);
    }

    /** Updates name, destination, price and duration. Seats and status are controlled by bookings. */
    public TravelPackage update(Long id, TravelPackage updated) {
        TravelPackage existing = findById(id);
        checkDestinationExists(updated.getDestinationId());
        existing.setPackageName(updated.getPackageName());
        existing.setDestinationId(updated.getDestinationId());
        existing.setPricePerPerson(updated.getPricePerPerson());
        existing.setDurationDays(updated.getDurationDays());
        return packageRepository.save(existing);
    }

    public void delete(Long id) {
        packageRepository.delete(findById(id));
    }

    /** Packages with more bookings than the average (subquery). */
    public List<PackageBookingStats> findPackagesAboveAverageBookings() {
        return bookingJdbcRepository.findPackagesAboveAverageBookings();
    }

    /** Package cost for a number of travelers (SQL function). */
    public CostResponse calculateCost(Long packageId, int travelers) {
        if (travelers < 1) {
            throw new BookingException("Number of travelers must be at least 1");
        }
        BigDecimal cost = bookingJdbcRepository.callCalculateCostFunction(packageId, travelers);
        if (cost == null) {
            throw new ResourceNotFoundException("Package not found with id " + packageId);
        }
        return new CostResponse(packageId, travelers, cost);
    }

    private void checkDestinationExists(Long destinationId) {
        if (!destinationRepository.existsById(destinationId)) {
            throw new ResourceNotFoundException("Destination not found with id " + destinationId);
        }
    }
}
