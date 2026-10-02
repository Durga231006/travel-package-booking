package com.travel.booking.service;

import com.travel.booking.dto.BookingDetails;
import com.travel.booking.dto.BookingRequest;
import com.travel.booking.dto.BookingResponse;
import com.travel.booking.exception.BookingException;
import com.travel.booking.repository.BookingJdbcRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookingService {

    private final BookingJdbcRepository bookingJdbcRepository;

    public BookingService(BookingJdbcRepository bookingJdbcRepository) {
        this.bookingJdbcRepository = bookingJdbcRepository;
    }

    public List<BookingDetails> findAllBookingDetails() {
        return bookingJdbcRepository.findAllBookingDetails();
    }

    /**
     * @Transactional keeps the whole procedure call in one transaction, so the row lock taken
     * by "FOR UPDATE" inside the procedure lasts until the booking and the trigger update finish.
     */
    @Transactional
    public BookingResponse bookPackage(BookingRequest request) {
        try {
            return bookingJdbcRepository.callBookPackageProcedure(
                    request.customerId(), request.packageId(), request.travelers());
        } catch (DataAccessException ex) {
            // Messages raised with SIGNAL inside the procedure (e.g. "Not enough seats ...") are shown to the user
            throw new BookingException(ex.getMostSpecificCause().getMessage());
        }
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        int updatedRows = bookingJdbcRepository.cancelBooking(bookingId);
        if (updatedRows == 0) {
            throw new BookingException("Booking not found or already cancelled");
        }
    }
}
