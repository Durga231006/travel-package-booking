package com.travel.booking.dto;

import java.math.BigDecimal;

/** Result returned by the stored procedure sp_book_package. */
public record BookingResponse(Long bookingId, BigDecimal totalCost, String message) {
}
