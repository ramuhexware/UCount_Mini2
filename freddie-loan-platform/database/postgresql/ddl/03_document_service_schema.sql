-- =============================================================================
-- Freddie Mac-Style Home Loan Platform
-- Database  : PostgreSQL 16
-- Schema    : freddie_customer  (document-service)
-- Script    : DDL – Schema, Tables, Indexes, Constraints
-- =============================================================================

\c freddie_customer;

CREATE SCHEMA IF NOT EXISTS freddie_customer;
SET search_path TO freddie_customer;

CREATE TABLE IF NOT EXISTS freddie_customer.loan_documents (
    document_id    VARCHAR(36)  NOT NULL,
    loan_id        VARCHAR(36)  NOT NULL,
    customer_id    VARCHAR(36)  NOT NULL,
    document_type  VARCHAR(50)  NOT NULL
                       CHECK (document_type IN ('W2','PAY_STUB','TAX_RETURN','APPRAISAL','ID_PROOF','BANK_STATEMENT','CREDIT_REPORT','PROPERTY_DEED')),
    file_name      VARCHAR(255) NOT NULL,
    mime_type      VARCHAR(100) NOT NULL,
    size_bytes     BIGINT,
    checksum       VARCHAR(64),
    file_data      BYTEA,                              -- Binary file storage replacing GridFS
    grid_fs_file_id VARCHAR(100),                      -- Legacy metadata compatibility
    status         VARCHAR(20)  NOT NULL DEFAULT 'UPLOADED'
                       CHECK (status IN ('UPLOADED','PROCESSING','VERIFIED','REJECTED','EXPIRED')),
    verified_by    VARCHAR(100),
    verified_at    TIMESTAMPTZ,
    expiry_date    DATE,
    metadata_json  TEXT,                              -- JSON metadata string
    uploaded_by    VARCHAR(100) NOT NULL,
    tags           VARCHAR(255),
    uploaded_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ,
    version        INT          DEFAULT 0,

    CONSTRAINT pk_loan_documents PRIMARY KEY (document_id)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_documents_loan_id     ON freddie_customer.loan_documents (loan_id);
CREATE INDEX IF NOT EXISTS idx_documents_customer_id ON freddie_customer.loan_documents (customer_id);
CREATE INDEX IF NOT EXISTS idx_documents_loan_type   ON freddie_customer.loan_documents (loan_id, document_type);
CREATE INDEX IF NOT EXISTS idx_documents_status      ON freddie_customer.loan_documents (status);
