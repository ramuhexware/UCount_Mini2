-- =============================================================================
-- Freddie Mac-Style Home Loan Platform
-- Database  : PostgreSQL 16
-- Schema    : freddie_loans / freddie_uw (underwriting-service)
-- Script    : DML – Seed Data
-- =============================================================================

\c freddie_loans;
SET search_path TO freddie_uw, freddie_loans;

INSERT INTO freddie_uw.underwriting_assessments (
    assessment_id, loan_id, customer_id, credit_score, dti_ratio, ltv_ratio, annual_income, monthly_debt, risk_level, decision, decision_reason, assessed_at, assessed_by, bureau_ref
) VALUES
('uw-1001', 'loan-10000001', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 730, 32.00, 81.82, 145000.00, 3866.67, 'LOW', 'APPROVED', 'DTI and LTV within tier-1 thresholds.', NOW(), 'auto-underwriter', 'EXP-884920'),
('uw-1002', 'loan-10000002', 'b2c3d4e5-f6a7-8901-bcde-f23456789012', 680, 41.50, 71.79, 92000.00, 3181.67, 'MEDIUM', 'REFERRED', 'DTI > 40%; manual underwriter review requested.', NOW(), 'auto-underwriter', 'EQF-392019')
ON CONFLICT (assessment_id) DO NOTHING;
