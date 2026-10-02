package com.travel.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** One row of the JOIN query: booking + customer + package + destination. */
public record BookingDetails(
        Long bookingId,
        String customerName,
        String customerEmail,
        String destinationName,
        String country,
        String packageName,
        Integer travelers,
        BigDecimal totalCost,
        LocalDateTime bookingDate,
        String status) {
}
