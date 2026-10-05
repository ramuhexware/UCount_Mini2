-- =============================================================================
-- Freddie Mac-Style Home Loan Platform
-- Database  : PostgreSQL 16
-- Schema    : freddie_loans  (loan-origination-service)
-- Script    : DDL – Schema, Tables, Indexes, Constraints
-- =============================================================================

\c freddie_loans;

CREATE SCHEMA IF NOT EXISTS freddie_loans;
SET search_path TO freddie_loans;

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- TABLE: loan_applications
CREATE TABLE IF NOT EXISTS freddie_loans.loan_applications (
    loan_id           VARCHAR(36)     NOT NULL,
    customer_id       VARCHAR(36)     NOT NULL,
    loan_type         VARCHAR(50)     NOT NULL
                          CHECK (loan_type IN ('PURCHASE','REFINANCE','HELOC','HOME_EQUITY')),
    loan_amount       NUMERIC(18,2)   NOT NULL,
    property_value    NUMERIC(18,2),
    property_address  VARCHAR(500),
    interest_rate     NUMERIC(6,4),
    loan_term_months  INT,
    loan_status       VARCHAR(30)     NOT NULL DEFAULT 'PENDING'
                          CHECK (loan_status IN
                              ('PENDING','SUBMITTED','UNDER_REVIEW','APPROVED',
                               'REJECTED','DISBURSED','CLOSED')),
    application_date  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    decision_date     TIMESTAMPTZ,
    disbursement_date TIMESTAMPTZ,
    approved_amount   NUMERIC(18,2),
    rejection_reason  VARCHAR(1000),
    created_by        VARCHAR(100),
    updated_at        TIMESTAMPTZ,
    version           BIGINT          NOT NULL DEFAULT 0,

    CONSTRAINT pk_loan_applications PRIMARY KEY (loan_id)
);

CREATE INDEX IF NOT EXISTS idx_loans_customer_id  ON freddie_loans.loan_applications (customer_id);
CREATE INDEX IF NOT EXISTS idx_loans_status       ON freddie_loans.loan_applications (loan_status);
CREATE INDEX IF NOT EXISTS idx_loans_type         ON freddie_loans.loan_applications (loan_type);
CREATE INDEX IF NOT EXISTS idx_loans_app_date     ON freddie_loans.loan_applications (application_date);

-- TABLE: loan_status_history
CREATE TABLE IF NOT EXISTS freddie_loans.loan_status_history (
    history_id   VARCHAR(36)     NOT NULL,
    loan_id      VARCHAR(36)     NOT NULL,
    from_status  VARCHAR(30),
    to_status    VARCHAR(30)     NOT NULL,
    changed_at   TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    changed_by   VARCHAR(100),
    notes        VARCHAR(2000),

    CONSTRAINT pk_loan_status_history PRIMARY KEY (history_id),
    CONSTRAINT fk_lsh_loan_application
        FOREIGN KEY (loan_id)
        REFERENCES freddie_loans.loan_applications (loan_id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_lsh_loan_id    ON freddie_loans.loan_status_history (loan_id);
CREATE INDEX IF NOT EXISTS idx_lsh_changed_at ON freddie_loans.loan_status_history (changed_at);

-- VIEW: active_loan_pipeline
CREATE OR REPLACE VIEW freddie_loans.active_loan_pipeline AS
SELECT
    la.loan_id,
    la.customer_id,
    la.loan_type,
    la.loan_amount,
    la.loan_status,
    la.application_date,
    ROUND(EXTRACT(EPOCH FROM (NOW() - la.application_date))/86400) AS days_in_pipeline
FROM
    freddie_loans.loan_applications la
WHERE
    la.loan_status IN ('SUBMITTED', 'UNDER_REVIEW');
