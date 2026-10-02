package com.travel.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Body of POST /api/bookings */
public record BookingRequest(
        @NotNull(message = "Customer is required") Long customerId,
        @NotNull(message = "Package is required") Long packageId,
        @NotNull(message = "Travelers is required") @Min(value = 1, message = "At least 1 traveler") Integer travelers) {
}
