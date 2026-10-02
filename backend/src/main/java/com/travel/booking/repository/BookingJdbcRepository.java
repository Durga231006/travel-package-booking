package com.travel.booking.repository;

import com.travel.booking.dto.BookingDetails;
import com.travel.booking.dto.BookingResponse;
import com.travel.booking.dto.PackageBookingStats;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Uses plain SQL through JdbcTemplate for everything that JPA cannot do easily:
 * the JOIN report, the subquery report, the stored procedure, the SQL function
 * and the cancel update (which fires a database trigger).
 */
@Repository
public class BookingJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public BookingJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Requirement 1: bookings with customer and destination details using JOIN. */
    public List<BookingDetails> findAllBookingDetails() {
        String sql = """
                SELECT b.booking_id, c.name AS customer_name, c.email AS customer_email,
                       d.name AS destination_name, d.country, p.package_name,
                       b.travelers, b.total_cost, b.booking_date, b.status
                FROM bookings b
                JOIN customers c    ON b.customer_id = c.customer_id
                JOIN packages p     ON b.package_id = p.package_id
                JOIN destinations d ON p.destination_id = d.destination_id
                ORDER BY b.booking_date DESC, b.booking_id DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new BookingDetails(
                rs.getLong("booking_id"),
                rs.getString("customer_name"),
                rs.getString("customer_email"),
                rs.getString("destination_name"),
                rs.getString("country"),
                rs.getString("package_name"),
                rs.getInt("travelers"),
                rs.getBigDecimal("total_cost"),
                rs.getTimestamp("booking_date").toLocalDateTime(),
                rs.getString("status")));
    }

    /**
     * Requirement 2: packages having more confirmed bookings than the average package.
     * The inner subquery counts bookings per package, the middle one averages those counts,
     * and HAVING keeps only packages above that average.
     */
    public List<PackageBookingStats> findPackagesAboveAverageBookings() {
        String sql = """
                SELECT p.package_id, p.package_name, d.name AS destination_name,
                       COUNT(b.booking_id) AS total_bookings
                FROM packages p
                JOIN destinations d ON p.destination_id = d.destination_id
                JOIN bookings b     ON b.package_id = p.package_id AND b.status = 'CONFIRMED'
                GROUP BY p.package_id, p.package_name, d.name
                HAVING COUNT(b.booking_id) > (
                    SELECT AVG(booking_count)
                    FROM (
                        SELECT COUNT(*) AS booking_count
                        FROM bookings
                        WHERE status = 'CONFIRMED'
                        GROUP BY package_id
                    ) AS counts_per_package
                )
                ORDER BY total_bookings DESC, p.package_name
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new PackageBookingStats(
                rs.getLong("package_id"),
                rs.getString("package_name"),
                rs.getString("destination_name"),
                rs.getLong("total_bookings")));
    }

    /** Requirement 3: calls the stored procedure sp_book_package. */
    public BookingResponse callBookPackageProcedure(Long customerId, Long packageId, Integer travelers) {
        Map<String, Object> row = jdbcTemplate.queryForMap(
                "CALL sp_book_package(?, ?, ?)", customerId, packageId, travelers);

        return new BookingResponse(
                ((Number) row.get("booking_id")).longValue(),
                (BigDecimal) row.get("total_cost"),
                (String) row.get("message"));
    }

    /** Requirement 4: calls the SQL function fn_calculate_package_cost. Returns null if package is missing. */
    public BigDecimal callCalculateCostFunction(Long packageId, int travelers) {
        return jdbcTemplate.queryForObject(
                "SELECT fn_calculate_package_cost(?, ?)", BigDecimal.class, packageId, travelers);
    }

    /** Cancels a confirmed booking. The trigger trg_bookings_after_update gives the seats back. */
    public int cancelBooking(Long bookingId) {
        return jdbcTemplate.update(
                "UPDATE bookings SET status = 'CANCELLED' WHERE booking_id = ? AND status = 'CONFIRMED'",
                bookingId);
    }
}
