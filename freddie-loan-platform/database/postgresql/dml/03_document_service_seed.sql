-- =============================================================================
-- Freddie Mac-Style Home Loan Platform
-- Database  : PostgreSQL 16
-- Schema    : freddie_customer  (document-service)
-- Script    : DML – Seed Data
-- =============================================================================

\c freddie_customer;
SET search_path TO freddie_customer;

INSERT INTO freddie_customer.loan_documents (
    document_id, loan_id, customer_id, document_type, file_name, mime_type, size_bytes, checksum, status, uploaded_by, uploaded_at, version
) VALUES
('doc-1001-w2', 'loan-10000001', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'W2', '2023_W2_Form.pdf', 'application/pdf', 245820, 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', 'VERIFIED', 'portal_user', NOW(), 1),
('doc-1002-paystub', 'loan-10000001', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'PAY_STUB', 'Paystub_Jan2024.pdf', 'application/pdf', 184300, 'f4c8996fb92427ae41e4649b934ca495991b7852b855e3b0c44298fc1c149a', 'VERIFIED', 'portal_user', NOW(), 1),
('doc-1003-appraisal', 'loan-10000002', 'b2c3d4e5-f6a7-8901-bcde-f23456789012', 'APPRAISAL', 'Property_Appraisal_742Evergreen.pdf', 'application/pdf', 4194304, '7ae41e4649b934ca495991b7852b855e3b0c44298fc1c149afbf4c8996fb924', 'PROCESSING', 'appraiser_svc', NOW(), 1)
ON CONFLICT (document_id) DO NOTHING;
