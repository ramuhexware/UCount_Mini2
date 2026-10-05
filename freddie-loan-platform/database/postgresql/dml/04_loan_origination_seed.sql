-- =============================================================================
-- Freddie Mac-Style Home Loan Platform
-- Database  : PostgreSQL 16
-- Schema    : freddie_loans  (loan-origination-service)
-- Script    : DML – Seed Data
-- =============================================================================

\c freddie_loans;
SET search_path TO freddie_loans;

INSERT INTO freddie_loans.loan_applications (
    loan_id, customer_id, loan_type, loan_amount, property_value,
    property_address, interest_rate, loan_term_months,
    loan_status, created_by, version
) VALUES
('loan-10000001', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'PURCHASE', 450000.00, 550000.00, '123 Maple Street, Austin, TX 78701', 6.8500, 360, 'APPROVED', 'system', 0),
('loan-10000002', 'b2c3d4e5-f6a7-8901-bcde-f23456789012', 'REFINANCE', 280000.00, 390000.00, '456 Oak Avenue, Phoenix, AZ 85001', 6.2500, 240, 'UNDER_REVIEW', 'system', 0),
('loan-10000003', 'c3d4e5f6-a7b8-9012-cdef-345678901234', 'HELOC', 100000.00, 750000.00, '789 Pine Road, Seattle, WA 98101', 7.1000, 120, 'SUBMITTED', 'system', 0)
ON CONFLICT (loan_id) DO NOTHING;

INSERT INTO freddie_loans.loan_status_history (
    history_id, loan_id, from_status, to_status, changed_by, notes
) VALUES
('lsh-10001', 'loan-10000001', NULL, 'PENDING', 'system', 'Loan application created.'),
('lsh-10002', 'loan-10000001', 'PENDING', 'SUBMITTED', 'customer-portal', 'Customer submitted application online.'),
('lsh-10003', 'loan-10000001', 'SUBMITTED', 'UNDER_REVIEW', 'underwriting-service', 'Routed to underwriting queue.'),
('lsh-10004', 'loan-10000001', 'UNDER_REVIEW', 'APPROVED', 'underwriter-jdoe', 'DTI 32%, LTV 81.8%, Credit Score 730. Approved.')
ON CONFLICT (history_id) DO NOTHING;
