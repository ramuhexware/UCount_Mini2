package com.freddieapp.origination.repository;

import com.freddieapp.origination.domain.LoanApplicationEntity;
import com.freddieapp.origination.domain.UcsAcsGrp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplicationEntity, Long> {

    List<LoanApplicationEntity> findByCustomerId(String customerId);

    Page<LoanApplicationEntity> findByStatus(String status, Pageable pageable);

    @Modifying
    @Query(value = """
            UPDATE loan_applications
            SET status = :status
            WHERE id = :loanId
            """, nativeQuery = true)
    int updateStatusNative(@Param("loanId") Long loanId, @Param("status") String status);

    @Query(value = """
            SELECT * FROM loan_applications
            WHERE status = 'ACTIVE'
            """, nativeQuery = true)
    List<LoanApplicationEntity> findAllActiveAccounts();

    @Query(value = """
            SELECT * FROM loan_applications
            WHERE customer_id = :orgId AND status = 'ACTIVE'
            """, nativeQuery = true)
    List<LoanApplicationEntity> findAllActiveAccountsOfOrg(@Param("orgId") String orgId);

    @Modifying
    @Query("UPDATE UcsOrgtnCrRtng c set c.dtOrgtnCrRtngExptn = :dtOrgtnCrRtngExptn, c.dttmLstUpdt = :dttmLstUpdt " +
           "where c.id.idOrgtn = :idOrgtn and c.id.idCrRtngAgency = :idCrRtngAgency and c.id.idCrRtngType = :idCrRtngType " +
           "and c.dtOrgtnCrRtngExptn is NULL")
    int updateUcsOrgtnCrRtng(
        @Param("dtOrgtnCrRtngExptn") java.util.Date dtOrgtnCrRtngExptn,
        @Param("dttmLstUpdt") java.util.Date dttmLstUpdt,
        @Param("idOrgtn") Integer idOrgtn,
        @Param("idCrRtngAgency") Integer idCrRtngAgency,
        @Param("idCrRtngType") Integer idCrRtngType
    );

    @Query(value = "SELECT DISTINCT rltnp.id_cntprty_acct, rltnp.id_rltd_cntprty_acct, rltnp.id_orgtn_role FROM ucs_cntprty_acct_rltnp rltnp", nativeQuery = true)
    List<Object[]> searchCounterpartyRelationships();

    @Modifying
    @Query(value = "INSERT INTO ucs_cntprty_acct_rltnp (id_cntprty_acct, id_rltd_cntprty_acct, id_orgtn_role, st_cntprty_acct_rltnp) " +
                   "VALUES (:idCntprty, :idRltd, :idRole, 'ACTIVE')", nativeQuery = true)
    int executeDirectDmlInsert(@Param("idCntprty") String idCntprty,
                              @Param("idRltd") String idRltd,
                              @Param("idRole") Integer idRole);

    @Modifying
    @Query(value = "INSERT INTO UCS_ORGTN_CNTCT (ID_ORGTN_CNTCT, ID_ORGTN, ID_INDVL) VALUES (:idOrgtnCntct, :idOrgtn, :idIndvl)", nativeQuery = true)
    int insertUcsOrgtnCntct(@Param("idOrgtnCntct") Object idOrgtnCntct,
                            @Param("idOrgtn") Object orgId,
                            @Param("idIndvl") Object idIndvl);

    @Query("SELECT COUNT(DISTINCT a) FROM UcsCntprtyAcct a")
    Long getDistinctCounterpartyAccountCount();

    @Query(name = "UcsAcsGrp.findAll")
    List<UcsAcsGrp> findAllAccessGroupsNamedQuery();

    @Query(value = """
            SELECT * FROM ((
            SELECT 'General' AS Category, la.id AS loan_id, la.customer_id, la.loan_amount,
            CASE WHEN la.loan_amount > 500000 THEN 'Y' ELSE 'N' END AS is_jumbo_loan
            FROM loan_applications la
            JOIN ucs_cntprty_acct ac ON la.customer_id = ac.id_cntprty_acct
            JOIN ucs_cntprty_acct_rltnp rltnp ON ac.id_cntprty_acct = rltnp.id_cntprty_acct
            LEFT JOIN ucs_acs_grp grp ON rltnp.id_orgtn_role = grp.id_acs_grp
            WHERE la.created_at = (SELECT MAX(tk.DTTM_CREATE) FROM UCS_APPL_TRKG tk WHERE tk.id_appl = la.customer_id)
            ) UNION (
            SELECT 'Special' AS Category, la.id AS loan_id, la.customer_id, la.loan_amount,
            CASE WHEN la.loan_amount > 750000 THEN 'Y' ELSE 'N' END AS is_jumbo_loan
            FROM loan_applications la
            JOIN ucs_cntprty_acct ac ON la.customer_id = ac.id_cntprty_acct
            JOIN ucs_cntprty_acct_rltnp rltnp ON ac.id_cntprty_acct = rltnp.id_cntprty_acct
            LEFT JOIN ucs_acs_grp grp ON rltnp.id_orgtn_role = grp.id_acs_grp
            WHERE rltnp.st_cntprty_acct_rltnp = 'ACTIVE'
            )) AS derived_summary
            """, nativeQuery = true)
    List<Object[]> getComplexMultiTableDerivedSummary();
}
