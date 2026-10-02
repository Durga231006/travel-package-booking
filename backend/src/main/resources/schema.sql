-- =====================================================================
-- Travel Package Booking - database objects (MySQL 8)
-- Statements are separated by a double dollar sign (see spring.sql.init.separator)
-- =====================================================================

-- ---------------------------- TABLES ---------------------------------

CREATE TABLE IF NOT EXISTS customers (
    customer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(120) NOT NULL UNIQUE,
    phone       VARCHAR(20),
    city        VARCHAR(80)
)$$

CREATE TABLE IF NOT EXISTS destinations (
    destination_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    country        VARCHAR(80)  NOT NULL,
    description    VARCHAR(500)
)$$

CREATE TABLE IF NOT EXISTS packages (
    package_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    package_name     VARCHAR(120)  NOT NULL,
    destination_id   BIGINT        NOT NULL,
    price_per_person DECIMAL(10,2) NOT NULL CHECK (price_per_person > 0),
    duration_days    INT           NOT NULL CHECK (duration_days > 0),
    total_seats      INT           NOT NULL CHECK (total_seats > 0),
    available_seats  INT           NOT NULL CHECK (available_seats >= 0),
    status           VARCHAR(20)   NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT fk_packages_destination
        FOREIGN KEY (destination_id) REFERENCES destinations (destination_id)
)$$

CREATE TABLE IF NOT EXISTS bookings (
    booking_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id  BIGINT        NOT NULL,
    package_id   BIGINT        NOT NULL,
    travelers    INT           NOT NULL CHECK (travelers > 0),
    total_cost   DECIMAL(12,2) NOT NULL,
    booking_date DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status       VARCHAR(20)   NOT NULL DEFAULT 'CONFIRMED',
    CONSTRAINT fk_bookings_customer FOREIGN KEY (customer_id) REFERENCES customers (customer_id),
    CONSTRAINT fk_bookings_package  FOREIGN KEY (package_id)  REFERENCES packages (package_id)
)$$

-- ---------------------------- FUNCTION -------------------------------
-- Calculates the cost of a package for a number of travelers.
-- Groups of 5 or more get a 10% discount. Returns NULL if the package does not exist.

DROP FUNCTION IF EXISTS fn_calculate_package_cost$$

CREATE FUNCTION fn_calculate_package_cost(p_package_id BIGINT, p_travelers INT)
RETURNS DECIMAL(12,2)
READS SQL DATA
BEGIN
    DECLARE v_price DECIMAL(10,2);
    DECLARE v_total DECIMAL(12,2);

    SELECT price_per_person INTO v_price
    FROM packages
    WHERE package_id = p_package_id;

    IF v_price IS NULL THEN
        RETURN NULL;
    END IF;

    SET v_total = v_price * p_travelers;

    IF p_travelers >= 5 THEN
        SET v_total = v_total * 0.90;
    END IF;

    RETURN ROUND(v_total, 2);
END$$

-- ---------------------------- PROCEDURE ------------------------------
-- Books a package: validates input, checks seats, calculates cost with the function,
-- inserts the booking and returns booking_id, total_cost and a message.
-- The trigger trg_bookings_after_insert then reduces the available seats.

DROP PROCEDURE IF EXISTS sp_book_package$$

CREATE PROCEDURE sp_book_package(
    IN p_customer_id BIGINT,
    IN p_package_id  BIGINT,
    IN p_travelers   INT
)
BEGIN
    DECLARE v_available  INT;
    DECLARE v_status     VARCHAR(20);
    DECLARE v_cost       DECIMAL(12,2);
    DECLARE v_booking_id BIGINT;

    IF p_travelers IS NULL OR p_travelers < 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Number of travelers must be at least 1';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM customers WHERE customer_id = p_customer_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Customer not found';
    END IF;

    -- FOR UPDATE locks the package row so two people cannot book the last seats together
    SELECT available_seats, status INTO v_available, v_status
    FROM packages
    WHERE package_id = p_package_id
    FOR UPDATE;

    IF v_available IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Package not found';
    END IF;

    IF v_status <> 'AVAILABLE' OR v_available < p_travelers THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Not enough seats available for this package';
    END IF;

    SET v_cost = fn_calculate_package_cost(p_package_id, p_travelers);

    INSERT INTO bookings (customer_id, package_id, travelers, total_cost, status)
    VALUES (p_customer_id, p_package_id, p_travelers, v_cost, 'CONFIRMED');

    SET v_booking_id = LAST_INSERT_ID();

    SELECT v_booking_id AS booking_id,
           v_cost       AS total_cost,
           'Booking confirmed successfully' AS message;
END$$

-- ---------------------------- TRIGGERS -------------------------------
-- After a booking is inserted: reduce seats and mark the package SOLD_OUT when seats reach 0.

DROP TRIGGER IF EXISTS trg_bookings_after_insert$$

CREATE TRIGGER trg_bookings_after_insert
AFTER INSERT ON bookings
FOR EACH ROW
BEGIN
    IF NEW.status = 'CONFIRMED' THEN
        UPDATE packages
        SET available_seats = available_seats - NEW.travelers
        WHERE package_id = NEW.package_id;

        UPDATE packages
        SET status = IF(available_seats <= 0, 'SOLD_OUT', 'AVAILABLE')
        WHERE package_id = NEW.package_id;
    END IF;
END$$

-- After a booking is cancelled: give the seats back and make the package AVAILABLE again.

DROP TRIGGER IF EXISTS trg_bookings_after_update$$

CREATE TRIGGER trg_bookings_after_update
AFTER UPDATE ON bookings
FOR EACH ROW
BEGIN
    IF OLD.status = 'CONFIRMED' AND NEW.status = 'CANCELLED' THEN
        UPDATE packages
        SET available_seats = available_seats + OLD.travelers,
            status = 'AVAILABLE'
        WHERE package_id = OLD.package_id;
    END IF;
END$$
