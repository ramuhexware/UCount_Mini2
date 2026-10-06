package com.freddieapp.origination.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

/**
 * Repository DAO implementing Dynamic Native SQL queries and Direct DML Inserts
 * using JPA EntityManager.createNativeQuery.
 */
@Repository
public class UcsDynamicNativeQueryDao {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Executes dynamic native SQL query built with dynamic criteria strings.
     * 
     * Pattern:
     * Query query = entityManager.createNativeQuery(
     *     "select distinct rltnp.id_cntprty_acct, rltnp.id_rltd_cntprty_acct, ... where ..." + searchCriteriaList,
     *     Object.class
     * );
     * return query.getResultList();
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> findCounterpartyRelationshipsDynamic(String searchCriteriaList) {
        String baseSql = "SELECT DISTINCT rltnp.id_cntprty_acct, rltnp.id_rltd_cntprty_acct, rltnp.id_orgtn_role, rltnp.st_cntprty_acct_rltnp " +
                         "FROM yu09.ucs_cntprty_acct_rltnp rltnp WHERE 1=1 ";
        
        if (searchCriteriaList != null && !searchCriteriaList.trim().isEmpty()) {
            baseSql += searchCriteriaList;
        }

        Query query = entityManager.createNativeQuery(baseSql, Object.class);
        return query.getResultList();
    }

    /**
     * Executes direct native SQL DML INSERT statement for Organization Contacts.
     * 
     * Pattern:
     * entityManager.createNativeQuery(
     *     "INSERT INTO UCS_ORGTN_CNTCT (ID_ORGTN_CNTCT, ID_ORGTN, ID_INDVL) VALUES (:idOrgtnCntct, :idOrgtn, :idIndvl)")
     *     .setParameter("idOrgtnCntct", idOrgtnCntct)
     *     .setParameter("idOrgtn", orgId)
     *     .setParameter("idIndvl", ucsOrgtnCntctTran.getIdIndvl())
     *     .executeUpdate();
     */
    @Transactional
    public int insertUcsOrgtnCntct(Object idOrgtnCntct, Object orgId, Object idIndvl) {
        return entityManager.createNativeQuery(
            "INSERT INTO yu09.ucs_orgtn_cntct (id_orgtn_cntct, id_orgtn, id_indvl) VALUES (:idOrgtnCntct, :idOrgtn, :idIndvl)")
            .setParameter("idOrgtnCntct", idOrgtnCntct)
            .setParameter("idOrgtn", orgId)
            .setParameter("idIndvl", idIndvl)
            .executeUpdate();
    }

    /**
     * Direct Native DML Insert for Control-M Job Execution records.
     */
    @Transactional
    public int insertControlMJob(Integer idCtrlMJob, String nameCtrlMJob, Timestamp dttmLstRun) {
        return entityManager.createNativeQuery(
            "INSERT INTO yu09.ucs_ctrl_m_job (id_ctrl_m_job, name_ctrl_m_job, dttm_ctrl_m_job_lst_scsfl_run) " +
            "VALUES (:idCtrlMJob, :nameCtrlMJob, :dttmLstRun)")
            .setParameter("idCtrlMJob", idCtrlMJob)
            .setParameter("nameCtrlMJob", nameCtrlMJob)
            .setParameter("dttmLstRun", dttmLstRun)
            .executeUpdate();
    }
}
