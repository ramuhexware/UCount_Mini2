-- =============================================================================
-- Freddie Mac-Style Home Loan Platform
-- Database  : PostgreSQL 16
-- Schema    : freddie_loans  (underwriting-service & struts-loan-portal)
-- Script    : DDL – Schema, Tables, Indexes, Constraints
-- =============================================================================

\c freddie_loans;

CREATE SCHEMA IF NOT EXISTS freddie_uw;
SET search_path TO freddie_uw, freddie_loans;

CREATE TABLE IF NOT EXISTS freddie_uw.underwriting_assessments (
    assessment_id    VARCHAR(36)      NOT NULL,
    loan_id          VARCHAR(36)      NOT NULL,
    customer_id      VARCHAR(36)      NOT NULL,
    credit_score     INT,
    dti_ratio        NUMERIC(5,2),
    ltv_ratio        NUMERIC(5,2),
    annual_income    NUMERIC(18,2),
    monthly_debt     NUMERIC(12,2),
    risk_level       VARCHAR(20)
                         CHECK (risk_level IN ('LOW','MEDIUM','HIGH','CRITICAL')),
    decision         VARCHAR(20)
                         CHECK (decision IN ('APPROVED','REFERRED','DECLINED')),
    decision_reason  VARCHAR(2000),
    assessed_at      TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    assessed_by      VARCHAR(100),
    bureau_ref       VARCHAR(255),

    CONSTRAINT pk_uw_assessments PRIMARY KEY (assessment_id)
);

CREATE INDEX IF NOT EXISTS idx_uw_loan_id      ON freddie_uw.underwriting_assessments (loan_id);
CREATE INDEX IF NOT EXISTS idx_uw_customer_id  ON freddie_uw.underwriting_assessments (customer_id);
CREATE INDEX IF NOT EXISTS idx_uw_decision     ON freddie_uw.underwriting_assessments (decision);
CREATE INDEX IF NOT EXISTS idx_uw_risk_level   ON freddie_uw.underwriting_assessments (risk_level);
CREATE INDEX IF NOT EXISTS idx_uw_assessed_at  ON freddie_uw.underwriting_assessments (assessed_at);

CREATE OR REPLACE VIEW freddie_uw.declined_assessments AS
SELECT
    assessment_id,
    loan_id,
    customer_id,
    credit_score,
    dti_ratio,
    ltv_ratio,
    risk_level,
    decision_reason,
    assessed_at
FROM
    freddie_uw.underwriting_assessments
WHERE
    decision = 'DECLINED'
ORDER BY
    assessed_at DESC;
