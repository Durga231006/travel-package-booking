package com.travel.booking.controller;

import com.travel.booking.dto.BookingDetails;
import com.travel.booking.dto.BookingRequest;
import com.travel.booking.dto.BookingResponse;
import com.travel.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /** JOIN report: bookings with customer and destination details. */
    @GetMapping
    public List<BookingDetails> getAllBookingDetails() {
        return bookingService.findAllBookingDetails();
    }

    /** Book a package using the stored procedure. */
    @PostMapping
    public ResponseEntity<BookingResponse> bookPackage(@Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.bookPackage(request));
    }

    @PutMapping("/{id}/cancel")
    public Map<String, String> cancel(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return Map.of("message", "Booking " + id + " cancelled");
    }
}
