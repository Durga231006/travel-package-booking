-- Sample data. INSERT IGNORE + fixed ids means it is safe to run on every start.
-- Bookings are inserted last, so the trigger reduces the seats of each package once.

INSERT IGNORE INTO customers (customer_id, name, email, phone, city) VALUES
 (1, 'Aarav Sharma', 'aarav@example.com', '9876500001', 'Chennai'),
 (2, 'Diya Patel',   'diya@example.com',  '9876500002', 'Mumbai'),
 (3, 'Rohan Iyer',   'rohan@example.com', '9876500003', 'Bengaluru'),
 (4, 'Meera Nair',   'meera@example.com', '9876500004', 'Kochi'),
 (5, 'Karan Singh',  'karan@example.com', '9876500005', 'Delhi')$$

INSERT IGNORE INTO destinations (destination_id, name, country, description) VALUES
 (1, 'Goa',     'India',     'Beaches, seafood and old Portuguese quarters'),
 (2, 'Munnar',  'India',     'Tea gardens and misty hills in Kerala'),
 (3, 'Bali',    'Indonesia', 'Temples, rice terraces and surf beaches'),
 (4, 'Dubai',   'UAE',       'Desert safaris, skyscrapers and shopping'),
 (5, 'Maldives','Maldives',  'Overwater villas and coral reefs')$$

INSERT IGNORE INTO packages
 (package_id, package_name, destination_id, price_per_person, duration_days, total_seats, available_seats, status) VALUES
 (1, 'Goa Beach Escape',        1, 12000.00, 4, 20, 20, 'AVAILABLE'),
 (2, 'Munnar Hills Retreat',    2, 15000.00, 3, 12, 12, 'AVAILABLE'),
 (3, 'Bali Culture and Surf',   3, 65000.00, 6, 10, 10, 'AVAILABLE'),
 (4, 'Dubai Desert Adventure',  4, 55000.00, 5,  8,  8, 'AVAILABLE'),
 (5, 'Maldives Luxury Stay',    5, 95000.00, 5,  6,  6, 'AVAILABLE'),
 (6, 'Goa Weekend Getaway',     1,  7000.00, 2, 15, 15, 'AVAILABLE')$$

INSERT IGNORE INTO bookings (booking_id, customer_id, package_id, travelers, total_cost, booking_date, status) VALUES
 (1, 1, 1, 2, fn_calculate_package_cost(1, 2), '2026-08-10 10:15:00', 'CONFIRMED'),
 (2, 2, 1, 3, fn_calculate_package_cost(1, 3), '2026-08-12 14:30:00', 'CONFIRMED'),
 (3, 3, 1, 1, fn_calculate_package_cost(1, 1), '2026-08-20 09:00:00', 'CONFIRMED'),
 (4, 4, 2, 2, fn_calculate_package_cost(2, 2), '2026-09-01 11:45:00', 'CONFIRMED'),
 (5, 5, 2, 4, fn_calculate_package_cost(2, 4), '2026-09-05 16:20:00', 'CONFIRMED'),
 (6, 1, 3, 2, fn_calculate_package_cost(3, 2), '2026-09-10 13:10:00', 'CONFIRMED'),
 (7, 2, 4, 5, fn_calculate_package_cost(4, 5), '2026-09-15 18:00:00', 'CONFIRMED'),
 (8, 3, 5, 1, fn_calculate_package_cost(5, 1), '2026-09-22 12:05:00', 'CONFIRMED')$$
