-- =============================================================================
-- Enterprise Standard DDL Script Pattern: CREATE OR REPLACE VIEW
-- Schema      : yu09
-- Description : DDL script using CREATE OR REPLACE VIEW with subquery projection,
--               row_number() OVER () AS "ROW_ID", uppercase column aliases,
--               pipe-delimited SEARCH_KEY with COALESCE, ownership assignment,
--               and full role grants (including TRUNCATE for readwrite).
-- =============================================================================

BEGIN;

SET ROLE yu09_ddlmgr;
SET SCHEMA 'yu09';

-- -----------------------------------------------------------------------------
-- VIEW: yu09.ucs_orgtn_quick_search_view
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW yu09.ucs_orgtn_quick_search_view
AS
SELECT row_number() OVER () AS "ROW_ID",
    r1."ORGANIZATION_ID",
    r1."INSTITUTION_NAME",
    r1."ACCOUNT_IDENTIFIER",
    r1."ACCOUNT_NAME",
    r1."CITY",
    r1."STATE",
    r1."SEARCH_KEY"
FROM ( SELECT t1.id_customer AS "ORGANIZATION_ID",
        concat(t1.name_first, ' ', t1.name_last) AS "INSTITUTION_NAME",
        t1.addr_email AS "ACCOUNT_IDENTIFIER",
        t1.num_phone AS "ACCOUNT_NAME",
        t1.stat_customer AS "CITY",
        t1.stat_kyc AS "STATE",
        concat(t1.id_customer::character varying(100), concat(' | ', concat(COALESCE(t1.name_first, ''::character varying), concat(' | ',
concat(COALESCE(t1.name_last, ''::character varying), concat(' | ', concat(COALESCE(t1.addr_email, ''::character varying), concat(' | ',
COALESCE(t1.stat_customer, ''::character varying))))))))) AS "SEARCH_KEY"
        FROM yu09.ucs_customer t1 ) r1;

ALTER TABLE yu09.ucs_orgtn_quick_search_view
    OWNER TO yu09_ddlmgr;

GRANT ALL ON TABLE yu09.ucs_orgtn_quick_search_view TO yu09_ddlmgr;
GRANT SELECT ON TABLE yu09.ucs_orgtn_quick_search_view TO yu09_readonly;
GRANT INSERT, SELECT, UPDATE, DELETE, TRUNCATE ON TABLE yu09.ucs_orgtn_quick_search_view TO yu09_readwrite;

-- -----------------------------------------------------------------------------
-- VIEW: yu09.ucs_loan_quick_search_view
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW yu09.ucs_loan_quick_search_view
AS
SELECT row_number() OVER () AS "ROW_ID",
    r1."LOAN_IDENTIFIER",
    r1."CUSTOMER_IDENTIFIER",
    r1."LOAN_TYPE",
    r1."LOAN_AMOUNT",
    r1."LOAN_STATUS",
    r1."SEARCH_KEY"
FROM ( SELECT t1.id_loan_application AS "LOAN_IDENTIFIER",
        t1.id_customer AS "CUSTOMER_IDENTIFIER",
        t1.type_loan AS "LOAN_TYPE",
        t1.amt_loan AS "LOAN_AMOUNT",
        t1.stat_loan AS "LOAN_STATUS",
        concat(t1.id_loan_application::character varying(100), concat(' | ', concat(COALESCE(t1.type_loan, ''::character varying), concat(' | ',
concat(COALESCE(t1.stat_loan, ''::character varying), concat(' | ', COALESCE(t1.amt_loan::text, ''::character varying))))))) AS "SEARCH_KEY"
        FROM yu09.ucs_loan_application t1 ) r1;

ALTER TABLE yu09.ucs_loan_quick_search_view
    OWNER TO yu09_ddlmgr;

GRANT ALL ON TABLE yu09.ucs_loan_quick_search_view TO yu09_ddlmgr;
GRANT SELECT ON TABLE yu09.ucs_loan_quick_search_view TO yu09_readonly;
GRANT INSERT, SELECT, UPDATE, DELETE, TRUNCATE ON TABLE yu09.ucs_loan_quick_search_view TO yu09_readwrite;

COMMIT;
