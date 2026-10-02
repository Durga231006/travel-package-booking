package com.travel.booking.exception;

/** Thrown for business rule problems such as no seats left (HTTP 400). */
public class BookingException extends RuntimeException {
    public BookingException(String message) {
        super(message);
    }
}
