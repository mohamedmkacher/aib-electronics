-- ============================================================================
-- AIB E-Commerce Platform - Additional Stored Procedures
-- Complete version with OUT parameters for all scalar-returning procedures
-- Run this script in MySQL after the existing routines are in place
-- ============================================================================

DELIMITER //

-- ============================================================================
-- ADDRESS REPOSITORY STORED PROCEDURES
-- ============================================================================

DROP PROCEDURE IF EXISTS sp_get_addresses_by_user_ordered//
CREATE PROCEDURE sp_get_addresses_by_user_ordered(
    IN p_user_id BIGINT
)
BEGIN
    SELECT * FROM addresses
    WHERE user_id = p_user_id
    ORDER BY is_default DESC, created_at DESC;
END//

DROP PROCEDURE IF EXISTS sp_get_default_address_by_user//
CREATE PROCEDURE sp_get_default_address_by_user(
    IN p_user_id BIGINT
)
BEGIN
    SELECT * FROM addresses
    WHERE user_id = p_user_id AND is_default = TRUE
    LIMIT 1;
END//

DROP PROCEDURE IF EXISTS sp_count_addresses_by_user//
CREATE PROCEDURE sp_count_addresses_by_user(
    IN p_user_id BIGINT,
    OUT p_count BIGINT
)
BEGIN
    SELECT COUNT(*) INTO p_count FROM addresses
    WHERE user_id = p_user_id;
END//

DROP PROCEDURE IF EXISTS sp_exists_address_by_id_and_user//
CREATE PROCEDURE sp_exists_address_by_id_and_user(
    IN p_id BIGINT,
    IN p_user_id BIGINT,
    OUT p_exists BOOLEAN
)
BEGIN
    SELECT COUNT(*) > 0 INTO p_exists FROM addresses
    WHERE id = p_id AND user_id = p_user_id;
END//

DROP PROCEDURE IF EXISTS sp_get_addresses_by_user//
CREATE PROCEDURE sp_get_addresses_by_user(
    IN p_user_id BIGINT
)
BEGIN
    SELECT * FROM addresses
    WHERE user_id = p_user_id;
END//

-- ============================================================================
-- CATEGORY REPOSITORY STORED PROCEDURES
-- ============================================================================

DROP PROCEDURE IF EXISTS sp_get_category_by_slug//
CREATE PROCEDURE sp_get_category_by_slug(
    IN p_slug VARCHAR(255)
)
BEGIN
    SELECT * FROM categories
    WHERE slug = p_slug;
END//

DROP PROCEDURE IF EXISTS sp_exists_category_by_name//
CREATE PROCEDURE sp_exists_category_by_name(
    IN p_name VARCHAR(255),
    OUT p_exists BOOLEAN
)
BEGIN
    SELECT COUNT(*) > 0 INTO p_exists FROM categories
    WHERE name = p_name;
END//

DROP PROCEDURE IF EXISTS sp_exists_category_by_name_and_id_not//
CREATE PROCEDURE sp_exists_category_by_name_and_id_not(
    IN p_name VARCHAR(255),
    IN p_id BIGINT,
    OUT p_exists BOOLEAN
)
BEGIN
    SELECT COUNT(*) > 0 INTO p_exists FROM categories
    WHERE name = p_name AND id != p_id;
END//

DROP PROCEDURE IF EXISTS sp_count_products_by_category//
CREATE PROCEDURE sp_count_products_by_category(
    IN p_category_id BIGINT,
    OUT p_count BIGINT
)
BEGIN
    SELECT COUNT(*) INTO p_count FROM products
    WHERE category_id = p_category_id;
END//

DROP PROCEDURE IF EXISTS sp_get_all_categories//
CREATE PROCEDURE sp_get_all_categories()
BEGIN
    SELECT * FROM categories
    ORDER BY display_order ASC, name ASC;
END//

DROP PROCEDURE IF EXISTS sp_get_active_categories//
CREATE PROCEDURE sp_get_active_categories()
BEGIN
    SELECT * FROM categories
    WHERE active = TRUE
    ORDER BY display_order ASC, name ASC;
END//

DROP PROCEDURE IF EXISTS sp_get_category_by_id//
CREATE PROCEDURE sp_get_category_by_id(
    IN p_id BIGINT
)
BEGIN
    SELECT * FROM categories
    WHERE id = p_id;
END//

-- ============================================================================
-- PASSWORD RESET TOKEN REPOSITORY STORED PROCEDURES
-- ============================================================================

DROP PROCEDURE IF EXISTS sp_get_password_reset_token_by_token//
CREATE PROCEDURE sp_get_password_reset_token_by_token(
    IN p_token VARCHAR(255)
)
BEGIN
    SELECT * FROM password_reset_tokens
    WHERE token = p_token;
END//

DROP PROCEDURE IF EXISTS sp_get_password_reset_token_by_user//
CREATE PROCEDURE sp_get_password_reset_token_by_user(
    IN p_user_id BIGINT
)
BEGIN
    SELECT * FROM password_reset_tokens
    WHERE user_id = p_user_id;
END//

DROP PROCEDURE IF EXISTS sp_delete_expired_password_reset_tokens//
CREATE PROCEDURE sp_delete_expired_password_reset_tokens(
    IN p_date DATETIME,
    OUT p_deleted_count INT
)
BEGIN
    DELETE FROM password_reset_tokens
    WHERE expiry_date < p_date;
    SET p_deleted_count = ROW_COUNT();
END//

-- ============================================================================
-- ROLE REPOSITORY STORED PROCEDURES
-- ============================================================================

DROP PROCEDURE IF EXISTS sp_get_role_by_name//
CREATE PROCEDURE sp_get_role_by_name(
    IN p_name VARCHAR(255)
)
BEGIN
    SELECT * FROM roles
    WHERE name = p_name;
END//

DROP PROCEDURE IF EXISTS sp_exists_role_by_name//
CREATE PROCEDURE sp_exists_role_by_name(
    IN p_name VARCHAR(255),
    OUT p_exists BOOLEAN
)
BEGIN
    SELECT COUNT(*) > 0 INTO p_exists FROM roles
    WHERE name = p_name;
END//

-- ============================================================================
-- VERIFICATION TOKEN REPOSITORY STORED PROCEDURES
-- ============================================================================

DROP PROCEDURE IF EXISTS sp_get_verification_token_by_token//
CREATE PROCEDURE sp_get_verification_token_by_token(
    IN p_token VARCHAR(255)
)
BEGIN
    SELECT * FROM verification_tokens
    WHERE token = p_token;
END//

DROP PROCEDURE IF EXISTS sp_get_verification_token_by_user//
CREATE PROCEDURE sp_get_verification_token_by_user(
    IN p_user_id BIGINT
)
BEGIN
    SELECT * FROM verification_tokens
    WHERE user_id = p_user_id;
END//

DROP PROCEDURE IF EXISTS sp_delete_verification_token_by_user//
CREATE PROCEDURE sp_delete_verification_token_by_user(
    IN p_user_id BIGINT,
    OUT p_deleted_count INT
)
BEGIN
    DELETE FROM verification_tokens
    WHERE user_id = p_user_id;
    SET p_deleted_count = ROW_COUNT();
END//

-- ============================================================================
-- ORDER ITEM REPOSITORY STORED PROCEDURES
-- ============================================================================

DROP PROCEDURE IF EXISTS sp_exists_order_item_by_product//
CREATE PROCEDURE sp_exists_order_item_by_product(
    IN p_product_id BIGINT,
    OUT p_exists BOOLEAN
)
BEGIN
    SELECT COUNT(*) > 0 INTO p_exists FROM order_items
    WHERE product_id = p_product_id;
END//

-- ============================================================================
-- PRODUCT REPOSITORY STORED PROCEDURES
-- ============================================================================

DROP PROCEDURE IF EXISTS sp_exists_product_by_sku//
CREATE PROCEDURE sp_exists_product_by_sku(
    IN p_sku VARCHAR(255),
    OUT p_exists BOOLEAN
)
BEGIN
    SELECT COUNT(*) > 0 INTO p_exists FROM products
    WHERE sku = p_sku;
END//

DROP PROCEDURE IF EXISTS sp_exists_product_by_sku_and_id_not//
CREATE PROCEDURE sp_exists_product_by_sku_and_id_not(
    IN p_sku VARCHAR(255),
    IN p_id BIGINT,
    OUT p_exists BOOLEAN
)
BEGIN
    SELECT COUNT(*) > 0 INTO p_exists FROM products
    WHERE sku = p_sku AND id != p_id;
END//

DROP PROCEDURE IF EXISTS sp_exists_product_by_name//
CREATE PROCEDURE sp_exists_product_by_name(
    IN p_name VARCHAR(255),
    OUT p_exists BOOLEAN
)
BEGIN
    SELECT COUNT(*) > 0 INTO p_exists FROM products
    WHERE name = p_name;
END//

DROP PROCEDURE IF EXISTS sp_exists_product_by_name_and_id_not//
CREATE PROCEDURE sp_exists_product_by_name_and_id_not(
    IN p_name VARCHAR(255),
    IN p_id BIGINT,
    OUT p_exists BOOLEAN
)
BEGIN
    SELECT COUNT(*) > 0 INTO p_exists FROM products
    WHERE name = p_name AND id != p_id;
END//

DROP PROCEDURE IF EXISTS sp_count_products_low_stock//
CREATE PROCEDURE sp_count_products_low_stock(
    IN p_threshold INT,
    OUT p_count BIGINT
)
BEGIN
    SELECT COUNT(*) INTO p_count FROM products
    WHERE stock_quantity < p_threshold;
END//

DROP PROCEDURE IF EXISTS sp_get_products_low_stock//
CREATE PROCEDURE sp_get_products_low_stock(
    IN p_threshold INT
)
BEGIN
    SELECT p.*
    FROM products p
             LEFT JOIN categories c ON p.category_id = c.id
    WHERE p.stock_quantity < p_threshold
    ORDER BY p.stock_quantity ASC;
END//

-- ============================================================================
-- USER REPOSITORY STORED PROCEDURES
-- ============================================================================

DROP PROCEDURE IF EXISTS sp_get_user_by_email//
CREATE PROCEDURE sp_get_user_by_email(
    IN p_email VARCHAR(255)
)
BEGIN
    SELECT * FROM users
    WHERE email = p_email;
END//

DROP PROCEDURE IF EXISTS sp_exists_user_by_email//
CREATE PROCEDURE sp_exists_user_by_email(
    IN p_email VARCHAR(255),
    OUT p_exists BOOLEAN
)
BEGIN
    SELECT COUNT(*) > 0 INTO p_exists FROM users
    WHERE email = p_email;
END//

DROP PROCEDURE IF EXISTS sp_exists_user_by_phone//
CREATE PROCEDURE sp_exists_user_by_phone(
    IN p_phone VARCHAR(255),
    OUT p_exists BOOLEAN
)
BEGIN
    SELECT COUNT(*) > 0 INTO p_exists FROM users
    WHERE phone = p_phone;
END//

DROP PROCEDURE IF EXISTS sp_exists_user_by_phone_and_id_not//
CREATE PROCEDURE sp_exists_user_by_phone_and_id_not(
    IN p_phone VARCHAR(255),
    IN p_id BIGINT,
    OUT p_exists BOOLEAN
)
BEGIN
    SELECT COUNT(*) > 0 INTO p_exists FROM users
    WHERE phone = p_phone AND id != p_id;
END//

DROP PROCEDURE IF EXISTS sp_get_user_by_provider//
CREATE PROCEDURE sp_get_user_by_provider(
    IN p_provider VARCHAR(255),
    IN p_provider_id VARCHAR(255)
)
BEGIN
    SELECT * FROM users
    WHERE provider = p_provider AND provider_id = p_provider_id;
END//

DROP PROCEDURE IF EXISTS sp_get_users_by_role_name//
CREATE PROCEDURE sp_get_users_by_role_name(
    IN p_role_name VARCHAR(255)
)
BEGIN
    SELECT u.* FROM users u
                        INNER JOIN roles r ON u.role_id = r.id
    WHERE r.name = p_role_name;
END//

DROP PROCEDURE IF EXISTS sp_count_users_after_date//
CREATE PROCEDURE sp_count_users_after_date(
    IN p_date DATETIME,
    OUT p_count BIGINT
)
BEGIN
    SELECT COUNT(*) INTO p_count FROM users
    WHERE created_at > p_date;
END//

DROP PROCEDURE IF EXISTS sp_count_non_admin_users//
CREATE PROCEDURE sp_count_non_admin_users(
    OUT p_count BIGINT
)
BEGIN
    SELECT COUNT(*) INTO p_count FROM users u
                                          INNER JOIN roles r ON u.role_id = r.id
    WHERE r.name != 'ROLE_ADMIN';
END//

DROP PROCEDURE IF EXISTS sp_count_non_admin_users_after//
CREATE PROCEDURE sp_count_non_admin_users_after(
    IN p_date DATETIME,
    OUT p_count BIGINT
)
BEGIN
    SELECT COUNT(*) INTO p_count FROM users u
                                          INNER JOIN roles r ON u.role_id = r.id
    WHERE r.name != 'ROLE_ADMIN' AND u.created_at > p_date;
END//

-- ============================================================================
-- CART REPOSITORY STORED PROCEDURES
-- ============================================================================

DROP PROCEDURE IF EXISTS sp_get_cart_by_session//
CREATE PROCEDURE sp_get_cart_by_session(
    IN p_session_id VARCHAR(255)
)
BEGIN
    SELECT * FROM carts
    WHERE session_id = p_session_id;
END//

DROP PROCEDURE IF EXISTS sp_get_cart_by_user_no_session//
CREATE PROCEDURE sp_get_cart_by_user_no_session(
    IN p_user_id BIGINT
)
BEGIN
    SELECT * FROM carts
    WHERE user_id = p_user_id AND session_id IS NULL;
END//

DROP PROCEDURE IF EXISTS sp_get_carts_by_product//
CREATE PROCEDURE sp_get_carts_by_product(
    IN p_product_id BIGINT
)
BEGIN
    SELECT DISTINCT c.* FROM carts c
                                 INNER JOIN cart_items ci ON c.id = ci.cart_id
    WHERE ci.product_id = p_product_id;
END//

-- ============================================================================
-- ORDER REPOSITORY STORED PROCEDURES
-- ============================================================================

DROP PROCEDURE IF EXISTS sp_get_orders_by_user_ordered//
CREATE PROCEDURE sp_get_orders_by_user_ordered(
    IN p_user_id BIGINT
)
BEGIN
    SELECT * FROM orders
    WHERE user_id = p_user_id
    ORDER BY created_at DESC;
END//

DROP PROCEDURE IF EXISTS sp_get_order_by_payment_intent//
CREATE PROCEDURE sp_get_order_by_payment_intent(
    IN p_payment_intent_id VARCHAR(255)
)
BEGIN
    SELECT * FROM orders
    WHERE stripe_payment_intent_id = p_payment_intent_id;
END//

-- Sum total by status (OUT parameter)
DROP PROCEDURE IF EXISTS sp_sum_total_by_status//
CREATE PROCEDURE sp_sum_total_by_status(
    IN p_status VARCHAR(50),
    OUT p_total DECIMAL(15,2)
)
BEGIN
    SELECT COALESCE(SUM(total), 0) INTO p_total FROM orders
    WHERE status = p_status;
END//

-- Sum total by status after date (OUT parameter)
DROP PROCEDURE IF EXISTS sp_sum_total_by_status_and_date_after//
CREATE PROCEDURE sp_sum_total_by_status_and_date_after(
    IN p_status VARCHAR(50),
    IN p_date DATETIME,
    OUT p_total DECIMAL(15,2)
)
BEGIN
    SELECT COALESCE(SUM(total), 0) INTO p_total FROM orders
    WHERE status = p_status AND created_at >= p_date;
END//

-- Sum total by status between dates (OUT parameter)
DROP PROCEDURE IF EXISTS sp_sum_total_by_status_and_date_between//
CREATE PROCEDURE sp_sum_total_by_status_and_date_between(
    IN p_status VARCHAR(50),
    IN p_start DATETIME,
    IN p_end DATETIME,
    OUT p_total DECIMAL(15,2)
)
BEGIN
    SELECT COALESCE(SUM(total), 0) INTO p_total FROM orders
    WHERE status = p_status AND created_at >= p_start AND created_at < p_end;
END//

-- Count orders after date (OUT parameter)
DROP PROCEDURE IF EXISTS sp_count_orders_after_date//
CREATE PROCEDURE sp_count_orders_after_date(
    IN p_date DATETIME,
    OUT p_count BIGINT
)
BEGIN
    SELECT COUNT(*) INTO p_count FROM orders
    WHERE created_at > p_date;
END//

-- Count orders between dates (OUT parameter)
DROP PROCEDURE IF EXISTS sp_count_orders_between_dates//
CREATE PROCEDURE sp_count_orders_between_dates(
    IN p_start DATETIME,
    IN p_end DATETIME,
    OUT p_count BIGINT
)
BEGIN
    SELECT COUNT(*) INTO p_count FROM orders
    WHERE created_at >= p_start AND created_at < p_end;
END//

-- Count non-cancelled orders total (OUT parameter)
DROP PROCEDURE IF EXISTS sp_count_non_cancelled_orders//
CREATE PROCEDURE sp_count_non_cancelled_orders(
    OUT p_count BIGINT
)
BEGIN
    SELECT COUNT(*) INTO p_count FROM orders
    WHERE status != 'CANCELLED';
END//

-- Count non-cancelled orders after date (OUT parameter)
DROP PROCEDURE IF EXISTS sp_count_non_cancelled_orders_after//
CREATE PROCEDURE sp_count_non_cancelled_orders_after(
    IN p_date DATETIME,
    OUT p_count BIGINT
)
BEGIN
    SELECT COUNT(*) INTO p_count FROM orders
    WHERE created_at >= p_date AND status != 'CANCELLED';
END//

-- Count non-cancelled orders between dates (OUT parameter)
DROP PROCEDURE IF EXISTS sp_count_non_cancelled_orders_between//
CREATE PROCEDURE sp_count_non_cancelled_orders_between(
    IN p_start DATETIME,
    IN p_end DATETIME,
    OUT p_count BIGINT
)
BEGIN
    SELECT COUNT(*) INTO p_count FROM orders
    WHERE created_at >= p_start AND created_at < p_end AND status != 'CANCELLED';
END//

-- Sum non-cancelled orders between dates (OUT parameter)
DROP PROCEDURE IF EXISTS sp_sum_non_cancelled_orders_between//
CREATE PROCEDURE sp_sum_non_cancelled_orders_between(
    IN p_start DATETIME,
    IN p_end DATETIME,
    OUT p_total DECIMAL(15,2)
)
BEGIN
    SELECT COALESCE(SUM(total), 0) INTO p_total FROM orders
    WHERE created_at >= p_start AND created_at < p_end AND status != 'CANCELLED';
END//

-- Get recent orders
DROP PROCEDURE IF EXISTS sp_get_recent_orders//
CREATE PROCEDURE sp_get_recent_orders(
    IN p_limit INT
)
BEGIN
    SELECT * FROM orders
    ORDER BY created_at DESC
    LIMIT p_limit;
END//

-- Count search orders (OUT parameter)
DROP PROCEDURE IF EXISTS sp_count_search_orders//
CREATE PROCEDURE sp_count_search_orders(
    IN p_order_number VARCHAR(255),
    IN p_customer_name VARCHAR(255),
    IN p_status VARCHAR(50),
    IN p_start_date DATETIME,
    IN p_end_date DATETIME,
    IN p_min_total DECIMAL(15,2),
    IN p_max_total DECIMAL(15,2),
    IN p_city VARCHAR(255),
    IN p_product_name VARCHAR(255),
    OUT p_count BIGINT
)
BEGIN
    SELECT COUNT(DISTINCT o.id) INTO p_count
    FROM orders o
             LEFT JOIN users u ON o.user_id = u.id
             LEFT JOIN addresses a ON o.shipping_address_id = a.id
             LEFT JOIN order_items oi ON o.id = oi.order_id
             LEFT JOIN products p ON oi.product_id = p.id
    WHERE (p_order_number IS NULL OR o.order_number LIKE CONCAT('%', p_order_number, '%'))
      AND (p_customer_name IS NULL OR CONCAT(u.first_name, ' ', u.last_name) LIKE CONCAT('%', p_customer_name, '%'))
      AND (p_status IS NULL OR o.status = p_status)
      AND (p_start_date IS NULL OR o.created_at >= p_start_date)
      AND (p_end_date IS NULL OR o.created_at <= p_end_date)
      AND (p_min_total IS NULL OR o.total >= p_min_total)
      AND (p_max_total IS NULL OR o.total <= p_max_total)
      AND (p_city IS NULL OR a.city LIKE CONCAT('%', p_city, '%'))
      AND (p_product_name IS NULL OR p.name LIKE CONCAT('%', p_product_name, '%'));
END//

-- Count search products (OUT parameter)
DROP PROCEDURE IF EXISTS sp_count_search_products//
CREATE PROCEDURE sp_count_search_products(
    IN p_keyword VARCHAR(255),
    IN p_category_id BIGINT,
    IN p_brand VARCHAR(255),
    IN p_min_price DECIMAL(15,2),
    IN p_max_price DECIMAL(15,2),
    IN p_in_stock BOOLEAN,
    IN p_has_discount BOOLEAN,
    IN p_is_featured BOOLEAN,
    IN p_active_only BOOLEAN,
    IN p_stock_status VARCHAR(50),
    OUT p_count BIGINT
)
BEGIN
    SELECT COUNT(*) INTO p_count
    FROM products p
             LEFT JOIN categories c ON p.category_id = c.id
    WHERE (p_keyword IS NULL OR p.name LIKE CONCAT('%', p_keyword, '%') OR p.description LIKE CONCAT('%', p_keyword, '%'))
      AND (p_category_id IS NULL OR p.category_id = p_category_id)
      AND (p_brand IS NULL OR p.brand = p_brand)
      AND (p_min_price IS NULL OR p.price >= p_min_price)
      AND (p_max_price IS NULL OR p.price <= p_max_price)
      AND (p_in_stock IS NULL OR (p_in_stock = TRUE AND p.stock_quantity > 0) OR (p_in_stock = FALSE))
      AND (p_has_discount IS NULL OR (p_has_discount = TRUE AND p.discount_percentage > 0) OR (p_has_discount = FALSE))
      AND (p_is_featured IS NULL OR p.featured = p_is_featured)
      AND (p_active_only IS NULL OR p_active_only = FALSE OR p.active = TRUE)
      AND (p_stock_status IS NULL OR
           (p_stock_status = 'IN_STOCK' AND p.stock_quantity > 10) OR
           (p_stock_status = 'LOW_STOCK' AND p.stock_quantity > 0 AND p.stock_quantity <= 10) OR
           (p_stock_status = 'OUT_OF_STOCK' AND p.stock_quantity = 0));
END//

-- ============================================================================
-- Reset delimiter
-- ============================================================================

DELIMITER ;