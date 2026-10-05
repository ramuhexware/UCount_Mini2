-- =============================================================================
-- Freddie Mac-Style Home Loan Platform
-- Database Setup: 2 PostgreSQL Databases Initialization Script
-- Databases: 1) freddie_customer  2) freddie_loans
-- =============================================================================

CREATE DATABASE freddie_customer;
CREATE DATABASE freddie_loans;

-- Ensure user freddie_admin has access to both databases
GRANT ALL PRIVILEGES ON DATABASE freddie_customer TO freddie_admin;
GRANT ALL PRIVILEGES ON DATABASE freddie_loans TO freddie_admin;
