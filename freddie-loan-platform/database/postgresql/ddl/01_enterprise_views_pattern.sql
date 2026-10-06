-- =============================================================================
-- Enterprise Standard View Script Pattern
-- Schema      : yu09
-- Description : Complete DDL script for Views adhering to standard transaction block,
--               role management, view dropping, creation with row_number(), formatting,
--               ownership assignment, and view-level role grants.
-- =============================================================================

BEGIN;

SET ROLE yu09_ddlmgr;
SET SCHEMA 'yu09';

-- -----------------------------------------------------------------------------
-- VIEW: yu09.ucs_srch_rprt_view
-- -----------------------------------------------------------------------------
DROP VIEW IF EXISTS yu09.ucs_srch_rprt_view;

CREATE VIEW yu09.ucs_srch_rprt_view AS
SELECT DISTINCT
    row_number() OVER () AS row_id,
    concat(concat(la.id_customer, ' - '), c.name_first, ' ', c.name_last) AS organization,
    CASE
        WHEN la.type_loan = 'PURCHASE' THEN 'Home Purchase'
        WHEN la.type_loan = 'REFINANCE' THEN 'Refinance Loan'
        WHEN la.type_loan = 'HELOC' THEN 'Home Equity Line'
        ELSE la.type_loan
    END AS reporttype,
    to_char(date(la.dttm_application)::timestamp with time zone, 'MM/DD/YYYY') AS submissiondate,
    CASE
        WHEN la.stat_loan = 'APPROVED' THEN 'Ready to Disburse'
        WHEN la.stat_loan = 'UNDER_REVIEW' THEN 'FM Review In Progress'
        ELSE la.stat_loan
    END AS status,
    uw.stat_decision AS analyst_decision,
    uw.val_credit_score AS credit_score,
    la.amt_loan AS loan_amount,
    la.dttm_create AS create_date,
    la.id_loan_application AS idappltrkg
FROM yu09.ucs_loan_application la
    JOIN yu09.ucs_customer c ON c.id_customer = la.id_customer
    LEFT JOIN yu09.ucs_kyc_record kr ON kr.id_customer = c.id_customer
    LEFT JOIN yu09.ucs_underwriting_assessment uw ON uw.id_loan_application = la.id_loan_application
ORDER BY la.dttm_create DESC;

ALTER TABLE yu09.ucs_srch_rprt_view
    OWNER TO yu09_ddlmgr;

GRANT ALL ON TABLE yu09.ucs_srch_rprt_view TO yu09_ddlmgr;
GRANT INSERT, SELECT, UPDATE, DELETE ON TABLE yu09.ucs_srch_rprt_view TO yu09_readwrite;
GRANT SELECT ON TABLE yu09.ucs_srch_rprt_view TO yu09_readonly;

-- -----------------------------------------------------------------------------
-- VIEW: yu09.ucs_active_pipeline_view
-- -----------------------------------------------------------------------------
DROP VIEW IF EXISTS yu09.ucs_active_pipeline_view;

CREATE VIEW yu09.ucs_active_pipeline_view AS
SELECT DISTINCT
    row_number() OVER () AS row_id,
    concat(concat(la.id_loan_application, ' - '), la.type_loan) AS loan_summary,
    c.addr_email AS customer_email,
    la.amt_loan AS requested_amount,
    la.stat_loan AS pipeline_status,
    to_char(date(la.dttm_application)::timestamp with time zone, 'MM/DD/YYYY') AS application_date,
    la.dttm_create AS create_date
FROM yu09.ucs_loan_application la
    JOIN yu09.ucs_customer c ON c.id_customer = la.id_customer
WHERE la.stat_loan IN ('PENDING', 'UNDER_REVIEW', 'APPROVED')
ORDER BY la.dttm_create DESC;

ALTER TABLE yu09.ucs_active_pipeline_view
    OWNER TO yu09_ddlmgr;

GRANT ALL ON TABLE yu09.ucs_active_pipeline_view TO yu09_ddlmgr;
GRANT INSERT, SELECT, UPDATE, DELETE ON TABLE yu09.ucs_active_pipeline_view TO yu09_readwrite;
GRANT SELECT ON TABLE yu09.ucs_active_pipeline_view TO yu09_readonly;

COMMIT;
