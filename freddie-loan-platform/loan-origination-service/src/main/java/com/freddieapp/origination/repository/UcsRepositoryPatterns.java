package com.freddieapp.origination.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

// =============================================================================
// Pattern 6: Named JPQL query declaration on entities
// =============================================================================
@Entity
@Table(name = "ucs_loan_application", schema = "yu09")
@NamedQuery(name = "UcsLoanApplication.findAll", query = "SELECT u FROM UcsLoanApplication u")
@NamedQuery(name = "UcsLoanApplication.findByStatus", query = "SELECT u FROM UcsLoanApplication u WHERE u.statLoan = :status")
class UcsLoanApplication implements Serializable {
    // Entity fields mapped to yu09 schema
    private Integer idLoanApplication;
    private Integer idCustomer;
    private String typeLoan;
    private String statLoan;
    private Timestamp dttmCreate;
    private Timestamp dttmLstUpdt;

    public Integer getIdLoanApplication() { return idLoanApplication; }
    public void setIdLoanApplication(Integer idLoanApplication) { this.idLoanApplication = idLoanApplication; }
    public Integer getIdCustomer() { return idCustomer; }
    public void setIdCustomer(Integer idCustomer) { this.idCustomer = idCustomer; }
    public String getTypeLoan() { return typeLoan; }
    public void setTypeLoan(String typeLoan) { this.typeLoan = typeLoan; }
    public String getStatLoan() { return statLoan; }
    public void setStatLoan(String statLoan) { this.statLoan = statLoan; }
    public Timestamp getDttmCreate() { return dttmCreate; }
    public void setDttmCreate(Timestamp dttmCreate) { this.dttmCreate = dttmCreate; }
    public Timestamp getDttmLstUpdt() { return dttmLstUpdt; }
    public void setDttmLstUpdt(Timestamp dttmLstUpdt) { this.dttmLstUpdt = dttmLstUpdt; }
}

@Repository
public interface UcsRepositoryPatterns extends JpaRepository<UcsLoanApplication, Integer> {

    // =============================================================================
    // Pattern 1: Spring Data derived query methods (method-name to SQL generation)
    // =============================================================================
    public Optional<UcsLoanApplication> findByIdCustomerAndTypeLoan(Integer idCustomer, String typeLoan);

    public Optional<UcsLoanApplication> findByIdCustomerAndStatLoan(Integer idCustomer, String statLoan);

    public UcsLoanApplication findTopByIdCustomerAndTypeLoanIgnoreCaseAndStatLoanIgnoreCaseOrderByDttmCreateDesc(
        Integer idCustomer, String typeLoan, String statLoan);

    // =============================================================================
    // Pattern 2: JPQL with @Query & @Modifying
    // =============================================================================
    @Query("SELECT c FROM UcsLoanApplication c WHERE c.statLoan = :status")
    public List<UcsLoanApplication> findByStatus(@Param("status") String status);

    @Modifying
    @Transactional
    @Query("UPDATE UcsLoanApplication c SET c.statLoan = :status, c.dttmLstUpdt = :dttmLstUpdt " +
           "WHERE c.idCustomer = :idCustomer AND c.idLoanApplication = :idLoanApplication")
    public void updateLoanStatus(@Param("idCustomer") Integer idCustomer,
                                 @Param("idLoanApplication") Integer idLoanApplication,
                                 @Param("status") String status,
                                 @Param("dttmLstUpdt") Timestamp dttmLstUpdt);

    // =============================================================================
    // Pattern 3: Native SQL via @Query(nativeQuery = true)
    // =============================================================================
    @Query(nativeQuery = true, value = "SELECT * FROM yu09.ucs_loan_application la " +
        "WHERE la.id_customer = :id AND la.type_loan = :type AND la.stat_loan = :statProcess " +
        "ORDER BY dttm_create DESC")
    public List<UcsLoanApplication> findUcsLoanApplicationList(@Param("id") int id,
                                                                @Param("type") String type,
                                                                @Param("statProcess") String statProcess);
}

// =============================================================================
// Pattern 4 & 5: Dynamic Native SQL & JPA Criteria API via Custom Repository / DAO
// =============================================================================
@Repository
class UcsCustomRepositoryImpl {

    @PersistenceContext
    private EntityManager entityManager;

    // -------------------------------------------------------------------------
    // Pattern 4: Dynamic native SQL through EntityManager.createNativeQuery
    // -------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public List<Object[]> findDynamicLoanReports(String searchCriteriaList) {
        jakarta.persistence.Query query = entityManager.createNativeQuery(
            "SELECT DISTINCT la.id_loan_application, la.id_customer, la.type_loan, la.stat_loan " +
            "FROM yu09.ucs_loan_application la WHERE 1=1 " + searchCriteriaList,
            Object.class
        );
        return query.getResultList();
    }

    @Transactional
    public void insertControlMJob(Integer idCtrlMJob, String jobName, Timestamp lastSuccessfulRun) {
        entityManager.createNativeQuery(
            "INSERT INTO yu09.ucs_ctrl_m_job (id_ctrl_m_job, name_ctrl_m_job, dttm_ctrl_m_job_lst_scsfl_run) " +
            "VALUES (:idCtrlMJob, :jobName, :lastSuccessfulRun)")
            .setParameter("idCtrlMJob", idCtrlMJob)
            .setParameter("jobName", jobName)
            .setParameter("lastSuccessfulRun", lastSuccessfulRun)
            .executeUpdate();
    }

    // -------------------------------------------------------------------------
    // Pattern 5: JPA Criteria API query construction
    // -------------------------------------------------------------------------
    public Long countDistinctApplications() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<UcsLoanApplication> root = countQuery.from(UcsLoanApplication.class);
        countQuery.select(cb.countDistinct(root));
        return entityManager.createQuery(countQuery).getSingleResult();
    }
}
