-- =============================================================================
-- Enterprise Standard DDL Script Pattern
-- Schema      : yu09
-- Description : Complete DDL script adhering to standard schema setting,
--               role management, role grants, table dropping, table creation,
--               column prefix conventions, and standard audit fields.
-- =============================================================================

SET SCHEMA 'yu09';

SET ROLE yu09_ddlmgr;

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA yu09 TO yu09_readwrite;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA yu09 TO yu09_ddlmgr;
GRANT SELECT ON ALL TABLES IN SCHEMA yu09 TO yu09_readonly;

-- -----------------------------------------------------------------------------
-- TABLE: ucs_ctrl_m_job
-- -----------------------------------------------------------------------------
drop table if exists ucs_ctrl_m_job;

CREATE TABLE ucs_ctrl_m_job(
    id_ctrl_m_job                  int4         NOT NULL,
    dttm_ctrl_m_job_lst_scsfl_run   timestamp    NOT NULL,
    name_ctrl_m_job                varchar(100) NOT NULL,
    dttm_create                    timestamp    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    dttm_lst_updt                  timestamp    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    id_user_create                 varchar(128) DEFAULT USER NOT NULL,
    id_user_lst_updt               varchar(128) DEFAULT USER NOT NULL,
    CONSTRAINT pk_ucs_ctrl_m_job PRIMARY KEY (id_ctrl_m_job)
);

-- -----------------------------------------------------------------------------
-- TABLE: ucs_customer
-- -----------------------------------------------------------------------------
drop table if exists ucs_customer;

CREATE TABLE ucs_customer(
    id_customer                    int4         NOT NULL,
    name_first                     varchar(100) NOT NULL,
    name_last                      varchar(100) NOT NULL,
    addr_email                     varchar(255) NOT NULL,
    num_phone                      varchar(20),
    stat_customer                  varchar(20)  DEFAULT 'ACTIVE' NOT NULL,
    stat_kyc                       varchar(20)  DEFAULT 'PENDING' NOT NULL,
    dttm_create                    timestamp    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    dttm_lst_updt                  timestamp    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    id_user_create                 varchar(128) DEFAULT USER NOT NULL,
    id_user_lst_updt               varchar(128) DEFAULT USER NOT NULL,
    CONSTRAINT pk_ucs_customer PRIMARY KEY (id_customer)
);

-- -----------------------------------------------------------------------------
-- TABLE: ucs_kyc_record
-- -----------------------------------------------------------------------------
drop table if exists ucs_kyc_record;

CREATE TABLE ucs_kyc_record(
    id_kyc_record                  int4         NOT NULL,
    id_customer                    int4         NOT NULL,
    name_kyc_provider              varchar(100),
    ref_kyc                        varchar(255),
    stat_kyc                       varchar(20)  NOT NULL,
    stat_risk_level                varchar(20),
    dttm_create                    timestamp    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    dttm_lst_updt                  timestamp    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    id_user_create                 varchar(128) DEFAULT USER NOT NULL,
    id_user_lst_updt               varchar(128) DEFAULT USER NOT NULL,
    CONSTRAINT pk_ucs_kyc_record PRIMARY KEY (id_kyc_record),
    CONSTRAINT fk_kyc_customer FOREIGN KEY (id_customer) REFERENCES ucs_customer(id_customer)
);

-- -----------------------------------------------------------------------------
-- TABLE: ucs_loan_application
-- -----------------------------------------------------------------------------
drop table if exists ucs_loan_application;

CREATE TABLE ucs_loan_application(
    id_loan_application            int4         NOT NULL,
    id_customer                    int4         NOT NULL,
    type_loan                      varchar(50)  NOT NULL,
    amt_loan                       numeric(18,2) NOT NULL,
    amt_property_value             numeric(18,2),
    stat_loan                      varchar(30)  DEFAULT 'PENDING' NOT NULL,
    dttm_application               timestamp    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    dttm_create                    timestamp    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    dttm_lst_updt                  timestamp    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    id_user_create                 varchar(128) DEFAULT USER NOT NULL,
    id_user_lst_updt               varchar(128) DEFAULT USER NOT NULL,
    CONSTRAINT pk_ucs_loan_application PRIMARY KEY (id_loan_application),
    CONSTRAINT fk_loan_customer FOREIGN KEY (id_customer) REFERENCES ucs_customer(id_customer)
);

-- -----------------------------------------------------------------------------
-- TABLE: ucs_loan_document
-- -----------------------------------------------------------------------------
drop table if exists ucs_loan_document;

CREATE TABLE ucs_loan_document(
    id_loan_document               int4         NOT NULL,
    id_loan_application            int4         NOT NULL,
    id_customer                    int4         NOT NULL,
    type_document                  varchar(50)  NOT NULL,
    name_file                      varchar(255) NOT NULL,
    mime_type                      varchar(100) NOT NULL,
    size_bytes                     int8,
    stat_document                  varchar(20)  DEFAULT 'UPLOADED' NOT NULL,
    dttm_create                    timestamp    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    dttm_lst_updt                  timestamp    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    id_user_create                 varchar(128) DEFAULT USER NOT NULL,
    id_user_lst_updt               varchar(128) DEFAULT USER NOT NULL,
    CONSTRAINT pk_ucs_loan_document PRIMARY KEY (id_loan_document),
    CONSTRAINT fk_doc_loan FOREIGN KEY (id_loan_application) REFERENCES ucs_loan_application(id_loan_application)
);

-- -----------------------------------------------------------------------------
-- TABLE: ucs_underwriting_assessment
-- -----------------------------------------------------------------------------
drop table if exists ucs_underwriting_assessment;

CREATE TABLE ucs_underwriting_assessment(
    id_uw_assessment               int4         NOT NULL,
    id_loan_application            int4         NOT NULL,
    id_customer                    int4         NOT NULL,
    val_credit_score               int4,
    val_dti_ratio                  numeric(5,2),
    val_ltv_ratio                  numeric(5,2),
    stat_risk_level                varchar(20),
    stat_decision                  varchar(20),
    dttm_create                    timestamp    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    dttm_lst_updt                  timestamp    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    id_user_create                 varchar(128) DEFAULT USER NOT NULL,
    id_user_lst_updt               varchar(128) DEFAULT USER NOT NULL,
    CONSTRAINT pk_ucs_uw_assessment PRIMARY KEY (id_uw_assessment),
    CONSTRAINT fk_uw_loan FOREIGN KEY (id_loan_application) REFERENCES ucs_loan_application(id_loan_application)
);
