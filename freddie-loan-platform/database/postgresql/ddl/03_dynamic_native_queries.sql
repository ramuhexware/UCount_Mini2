-- =============================================================================
-- Enterprise Standard SQL Reference Script: Dynamic Native SQL Queries & Direct DML Inserts
-- Schema      : yu09
-- Description : SQL templates executed dynamically by EntityManager.createNativeQuery
-- =============================================================================

SET SCHEMA 'yu09';

-- -----------------------------------------------------------------------------
-- 1. Dynamic Search Query Template (Used with dynamic WHERE clause builder)
-- -----------------------------------------------------------------------------
-- Base SQL String for dynamic concatenated criteria:
-- SELECT DISTINCT rltnp.id_cntprty_acct, rltnp.id_rltd_cntprty_acct, rltnp.id_orgtn_role, rltnp.st_cntprty_acct_rltnp
-- FROM yu09.ucs_cntprty_acct_rltnp rltnp
-- WHERE 1=1 [ + searchCriteriaList ]

SELECT DISTINCT 
    rltnp.id_cntprty_acct, 
    rltnp.id_rltd_cntprty_acct, 
    rltnp.id_orgtn_role, 
    rltnp.st_cntprty_acct_rltnp
FROM yu09.ucs_cntprty_acct_rltnp rltnp
WHERE 1=1
  AND rltnp.st_cntprty_acct_rltnp = 'ACTIVE'
ORDER BY rltnp.id_cntprty_acct;

-- -----------------------------------------------------------------------------
-- 2. Direct Native DML Insert Statement Template
-- -----------------------------------------------------------------------------
-- Executed via: entityManager.createNativeQuery(SQL).setParameter(...).executeUpdate();

INSERT INTO yu09.ucs_orgtn_cntct (
    id_orgtn_cntct, 
    id_orgtn, 
    id_indvl,
    dttm_create,
    dttm_lst_updt,
    id_user_create,
    id_user_lst_updt
) VALUES (
    :idOrgtnCntct, 
    :idOrgtn, 
    :idIndvl,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    USER,
    USER
);

-- -----------------------------------------------------------------------------
-- 3. Control-M Job Log Direct Native DML Insert
-- -----------------------------------------------------------------------------
INSERT INTO yu09.ucs_ctrl_m_job (
    id_ctrl_m_job,
    name_ctrl_m_job,
    dttm_ctrl_m_job_lst_scsfl_run,
    dttm_create,
    dttm_lst_updt,
    id_user_create,
    id_user_lst_updt
) VALUES (
    :idCtrlMJob,
    :nameCtrlMJob,
    :dttmLstRun,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    USER,
    USER
);
