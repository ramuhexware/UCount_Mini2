package com.freddieapp.origination.repository;

import com.freddieapp.origination.domain.LoanApplicationEntity;
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
}
