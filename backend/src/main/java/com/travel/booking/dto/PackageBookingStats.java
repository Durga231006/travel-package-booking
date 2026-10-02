package com.travel.booking.dto;

/** One row of the subquery report: a package with more bookings than average. */
public record PackageBookingStats(
        Long packageId,
        String packageName,
        String destinationName,
        Long totalBookings) {
}
