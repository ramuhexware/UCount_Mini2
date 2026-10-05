package com.freddieapp.origination.service;

import com.freddieapp.origination.domain.LoanApplicationEntity;
import com.freddieapp.origination.domain.UcsAcsGrp;
import com.freddieapp.origination.domain.UcsCntprtyAcct;
import com.freddieapp.origination.dto.LoanDTOs.*;
import com.freddieapp.origination.processor.UnderwritingRuleProcessor;
import com.freddieapp.origination.repository.LoanApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Core Business Service Layer for Mortgage Origination, Counterparty Intake,
 * Account Management, and Automated Underwriting.
 */
@Service
@Transactional
public class LoanOriginationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoanOriginationService.class);
    private final LoanApplicationRepository repository;
    private final UnderwritingRuleProcessor underwritingRuleProcessor;
    private final WebClient webClient;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${dataServiceURL:http://localhost:8082/api/v1}")
    private String dataServiceURL;

    @Value("${byPassOimSync:true}")
    private boolean byPassOimSync;

    // In-memory data store for Stage 1/2 Counterparty Intake
    private final Map<String, Stage1UserResponseDTO> stage1Users = new ConcurrentHashMap<>();
    private final Map<String, Stage2ProfileRequestDTO> stage2Profiles = new ConcurrentHashMap<>();

    @Autowired
    public LoanOriginationService(LoanApplicationRepository repository,
                                 UnderwritingRuleProcessor underwritingRuleProcessor,
                                 WebClient webClient) {
        this.repository = repository;
        this.underwritingRuleProcessor = underwritingRuleProcessor;
        this.webClient = webClient;
    }

    public LoanResponseDTO createLoanApplication(LoanRequestDTO request) {
        LoanApplicationEntity entity = new LoanApplicationEntity();
        entity.setCustomerId(request.customerId());
        entity.setApplicantName(request.applicantName());
        entity.setEmail(request.email());
        entity.setLoanAmount(request.loanAmount());
        entity.setPropertyValue(request.propertyValue());
        entity.setMonthlyIncome(request.monthlyIncome());
        entity.setMonthlyDebt(request.monthlyDebt());
        entity.setCreditScore(request.creditScore());
        entity.setTermMonths(request.termMonths() != null ? request.termMonths() : 360);

        LoanApplicationEntity saved = repository.save(entity);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public LoanResponseDTO getLoanById(Long loanId) {
        LoanApplicationEntity entity = repository.findById(loanId)
            .orElseThrow(() -> new UcsApiException(HttpStatus.NOT_FOUND, "Loan application not found with ID: " + loanId));
        return mapToResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<LoanResponseDTO> getLoansByCustomerId(String customerId) {
        return repository.findByCustomerId(customerId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public AssessmentResultDTO submitForUnderwritingNative(Long loanId) {
        int rowsUpdated = repository.updateStatusNative(loanId, "UNDER_REVIEW");
        if (rowsUpdated == 0) {
            throw new UcsApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update status via PostgreSQL native query for ID: " + loanId);
        }

        LoanApplicationEntity entity = repository.findById(loanId)
            .orElseThrow(() -> new UcsApiException(HttpStatus.NOT_FOUND, "Loan not found: " + loanId));

        AssessmentRequestDTO req = new AssessmentRequestDTO(
            entity.getId(),
            entity.getCustomerId(),
            entity.getLoanAmount(),
            entity.getPropertyValue(),
            entity.getMonthlyIncome(),
            entity.getMonthlyDebt(),
            entity.getCreditScore(),
            entity.getTermMonths()
        );

        AssessmentResultDTO result = underwritingRuleProcessor.evaluateRisk(req);
        repository.updateStatusNative(loanId, result.decision());
        return result;
    }

    public PricingQuoteDTO getPricingQuote(Integer creditScore, BigDecimal ltvRatio) {
        return underwritingRuleProcessor.calculatePricingQuote(creditScore, ltvRatio);
    }

    public AmortizationScheduleDTO getAmortizationSchedule(BigDecimal loanAmount, BigDecimal annualRate, int termMonths) {
        return underwritingRuleProcessor.generateAmortizationSchedule(loanAmount, annualRate, termMonths);
    }

    // Account Lookup Data
    public AccountLookupDTO getLookupData() {
        return new AccountLookupDTO(
            List.of("MORTGAGE_ORIGINATION_ACCT", "UNDERWRITING_ACCT", "SECONDARY_MARKET_ACCT"),
            List.of("PRIMARY_LENDER", "BROKER", "SERVICER", "CORRESPONDENT"),
            Map.of("SYS_STATUS", "ACTIVE", "REGION", "US_EAST")
        );
    }

    public AccountLookupUpdateDTO getLookupUpdateData() {
        AccountLookupUpdateDTO accountLookupUpdateDTO = new AccountLookupUpdateDTO();

        List<UcsLineOfBusinessDTO> listUcsLineOfBusiness = List.of(
            new UcsLineOfBusinessDTO(1, "Mortgage Origination"),
            new UcsLineOfBusinessDTO(2, "Underwriting & Risk"),
            new UcsLineOfBusinessDTO(3, "Secondary Market")
        );

        accountLookupUpdateDTO.setUcsLineOfBusinessDTOs(listUcsLineOfBusiness);
        accountLookupUpdateDTO.setUcsCntprtyAcctSt(List.of("ACTIVE", "PENDING", "SUSPENDED"));
        accountLookupUpdateDTO.setUcsCntprtyAcctRoleSt(List.of("PRIMARY", "SECONDARY", "AUDITOR"));
        accountLookupUpdateDTO.setRespSts(new ResponseStatusDTO(200, "Account lookup data retrieved successfully"));
        return accountLookupUpdateDTO;
    }

    public AccountSaveDTO createAccount(AccountSaveDTO accountReq) {
        String idCntprtyAcct = (accountReq.getIdCntprtyAcct() != null)
            ? accountReq.getIdCntprtyAcct()
            : "ACC-" + (100000 + new Random().nextInt(900000));
        accountReq.setIdCntprtyAcct(idCntprtyAcct);

        expireAccountRelationship(1001, accountReq);
        accountReq.setRespSts(new ResponseStatusDTO(200, "Account Created Successfully"));
        return accountReq;
    }

    private void expireAccountRelationship(int idOrgtnRole, AccountSaveDTO accountReq) {
        List<Short> expirableRelationshipsList = List.of((short) 25, (short) 30);
        List<Short> activeRelationships = List.of((short) 25, (short) 10);

        for (Short relationId : activeRelationships) {
            if (expirableRelationshipsList.contains(relationId)) {
                if (relationId == 25) {
                    LOGGER.info("SELLER-SERVICER-DISCONTINUE-CTOS - Seller-Ctos Servicer Relationship exists: {}", accountReq.getIdCntprtyAcct());
                }
            }
        }
    }

    public AccountProfileRespDTO getAccountProfile(AccountProfileReqDTO accountProfileReqDTO) {
        return new AccountProfileRespDTO(
            accountProfileReqDTO.idCntprtyAcct(),
            "VERIFIED_ACTIVE",
            List.of("LOAN_ORIGINATION_PORTAL:FULL", "APPRAISAL_PORTAL:READ", "TITLE_PORTAL:READ")
        );
    }

    // Stage 1 Intake & Onboarding
    public Stage1UserResponseDTO onboardStage1User(Stage1OnboardRequestDTO req) {
        String userId = "USR-" + (100000 + new Random().nextInt(900000));
        Stage1UserResponseDTO resp = new Stage1UserResponseDTO(userId, req.orgName(), req.email(), Stage1Status.PENDING_APPROVAL, LocalDateTime.now());
        stage1Users.put(userId, resp);
        return resp;
    }

    public Stage1UserResponseDTO approveStage1User(String userId) {
        Stage1UserResponseDTO existing = stage1Users.get(userId);
        if (existing == null) {
            existing = new Stage1UserResponseDTO(userId, "Freddie Partner Org", "user@partner.com", Stage1Status.PENDING_APPROVAL, LocalDateTime.now());
        }
        Stage1UserResponseDTO approved = new Stage1UserResponseDTO(userId, existing.orgName(), existing.email(), Stage1Status.APPROVED, LocalDateTime.now());
        stage1Users.put(userId, approved);
        return approved;
    }

    public List<Stage1UserResponseDTO> getPendingStage1Users() {
        return new ArrayList<>(stage1Users.values());
    }

    // Stage 2 Extended Profile & Access Rights
    public Stage2AccessRightsResponseDTO saveStage2Profile(Stage2ProfileRequestDTO req) {
        stage2Profiles.put(req.userId(), req);
        List<String> rights = evaluateAccessRights(req.userType());
        return new Stage2AccessRightsResponseDTO(req.userId(), req.userType(), rights);
    }

    public Stage2AccessRightsResponseDTO getStage2AccessRights(String userId) {
        Stage2ProfileRequestDTO req = stage2Profiles.get(userId);
        UserType userType = (req != null) ? req.userType() : UserType.HOUSE_BUYER;
        return new Stage2AccessRightsResponseDTO(userId, userType, evaluateAccessRights(userType));
    }

    private List<String> evaluateAccessRights(UserType userType) {
        return switch (userType) {
            case HOUSE_SELLER -> List.of("LOAN_ORIGINATION_PORTAL:FULL", "APPRAISAL_PORTAL:READ", "TITLE_PORTAL:READ");
            case HOUSE_BUYER -> List.of("LOAN_ORIGINATION_PORTAL:FULL", "CUSTOMER_PORTAL:FULL", "CARD_SERVICE_PORTAL:READ");
            case INSURANCE_PERSON -> List.of("TITLE_PORTAL:FULL", "DOCUMENT_SERVICE:READ", "ESCROW_PORTAL:READ");
            case MORTGAGE_SERVICER -> List.of("LOAN_SERVICING_PORTAL:FULL", "SECONDARY_MARKET_ACCESS:FULL", "REPORT_PORTAL:READ");
        };
    }

    private LoanResponseDTO mapToResponse(LoanApplicationEntity e) {
        return new LoanResponseDTO(
            e.getId(),
            e.getCustomerId(),
            e.getApplicantName(),
            e.getEmail(),
            e.getLoanAmount(),
            e.getPropertyValue(),
            e.getCreditScore(),
            e.getStatus(),
            e.getCreatedAt()
        );
    }

    // Java 17 Multiline Text Blocks Feature Integration
    public List<TextBlockDTO> getTextBlocks() {
        return List.of(
            new TextBlockDTO(
                "TILA_DISCLOSURE",
                "Truth in Lending Act (TILA) Initial Disclosure",
                "COMPLIANCE_LEGAL",
                """
                FEDERAL TRUTH IN LENDING DISCLOSURE STATEMENT
                ====================================================================
                1. ANNUAL PERCENTAGE RATE (APR): The cost of your credit as a yearly rate.
                2. FINANCE CHARGE: The dollar amount the credit will cost you.
                3. AMOUNT FINANCED: The amount of credit provided to you or on your behalf.
                4. TOTAL OF PAYMENTS: The amount you will have paid after making all scheduled payments.
                --------------------------------------------------------------------
                Notice: You are not required to complete this agreement merely because you have
                received these disclosures or signed an application.
                ====================================================================""",
                "Standard federal TILA disclosure provided to mortgage applicants upon origination."
            ),
            new TextBlockDTO(
                "ECOA_NOTICE",
                "Equal Credit Opportunity Act (ECOA) Notice",
                "REGULATORY_RIGHTS",
                """
                EQUAL CREDIT OPPORTUNITY ACT NOTICE
                ====================================================================
                The Federal Equal Credit Opportunity Act prohibits creditors from discriminating
                against credit applicants on the basis of race, color, religion, national origin,
                sex, marital status, age, or because all or part of the applicant's income derives
                from any public assistance program.
                --------------------------------------------------------------------
                The Federal Agency that administers compliance with this law concerning this creditor is:
                Consumer Financial Protection Bureau (CFPB), 1700 G Street NW, Washington, DC 20006.
                ====================================================================""",
                "Mandatory fair lending disclosure document presented to all loan originators and buyers."
            ),
            new TextBlockDTO(
                "UNDERWRITING_NOTES",
                "Automated Underwriting System (AUS) Decision Notes",
                "UNDERWRITING_RULES",
                """
                FREDDIE MAC AUTOMATED UNDERWRITING EVALUATION SUMMARY
                ====================================================================
                Criteria Checked:
                  - Debt-To-Income (DTI) Ratio  : Threshold <= 45.0%
                  - Loan-To-Value (LTV) Ratio  : Threshold <= 80.0% (No PMI required)
                  - FICO Credit Risk Tier      : Prime (750+), Near-Prime (650-749)
                --------------------------------------------------------------------
                Evaluation Rule Pattern: Java 17 Switch Pattern Risk Engine
                Primary Market Eligibility : Eligible for Single-Family Purchased Mortgages
                ====================================================================""",
                "Multi-line summary block of underwriting decision rules applied during evaluation."
            ),
            new TextBlockDTO(
                "POSTGRES_SQL_BLOCK",
                "Native PostgreSQL State Transition Text Block",
                "SYSTEM_DATABASE",
                """
                UPDATE loan_applications
                SET status = :status,
                    updated_at = NOW()
                WHERE id = :loanId
                  AND status != 'ARCHIVED';""",
                "Native PostgreSQL multi-line SQL statement used in JPA LoanApplicationRepository."
            ),
            new TextBlockDTO(
                "PDF_TEMPLATE_BLOCK",
                "Loan Summary PDF Exporter Document Structure",
                "DOCUMENT_EXPORTER",
                """
                %PDF-1.4
                ------------------------------------------------------------------
                FREDDIE MAC HOME LOAN PLATFORM - MORTGAGE SUMMARY DISCLOSURE
                ------------------------------------------------------------------
                Application ID : LN-%d
                Borrower Name  : %s
                Loan Amount    : $%s
                ------------------------------------------------------------------
                EQUAL HOUSING OPPORTUNITY - FREDDIE MAC ENTERPRISE""",
                "Java 17 text block format string template for binary PDF stream generation."
            ),
            new TextBlockDTO(
                "UCS_ORGTN_CR_RTNG_QUERY",
                "Organization Credit Rating Update Query",
                "SYSTEM_DATABASE",
                """
                UPDATE ucs_orgtn_cr_rtng
                SET dt_orgtn_cr_rtng_exptn = :dtOrgtnCrRtngExptn,
                    dttm_lst_updt = :dttmLstUpdt
                WHERE id_orgtn = :idOrgtn
                  AND id_cr_rtng_agency = :idCrRtngAgency
                  AND id_cr_rtng_type = :idCrRtngType
                  AND dt_orgtn_cr_rtng_exptn IS NULL""",
                "Native SQL update query for expiring active organization credit ratings."
            )
        );
    }

    public int updateUcsOrgtnCrRtng(Date dtExptn, Date dttmLstUpdt, Integer idOrgtn, Integer idAgency, Integer idType) {
        return repository.updateUcsOrgtnCrRtng(dtExptn, dttmLstUpdt, idOrgtn, idAgency, idType);
    }

    public TextBlockDTO getTextBlockByCode(String code) {
        return getTextBlocks().stream()
            .filter(tb -> tb.code().equalsIgnoreCase(code))
            .findFirst()
            .orElseThrow(() -> new UcsApiException(HttpStatus.NOT_FOUND, "Text block not found for code: " + code));
    }

    // Dynamic Native SQL through EntityManager.createNativeQuery for complex dynamic filters
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Object[]> searchCounterpartyRelationships(String searchCriteriaList) {
        String sql = "select distinct rltnp.id_cntprty_acct, rltnp.id_rltd_cntprty_acct, rltnp.id_orgtn_role " +
                     "from ucs_cntprty_acct_rltnp rltnp where 1=1 " + (searchCriteriaList != null ? searchCriteriaList : "");
        Query query = entityManager.createNativeQuery(sql);
        return query.getResultList();
    }

    // Dynamic Native SQL through EntityManager.createNativeQuery for direct DML inserts
    @Transactional
    public int executeDirectDmlInsert(String idCntprtyAcct, String idRltdCntprtyAcct, Integer idOrgtnRole) {
        String insertSql = "INSERT INTO ucs_cntprty_acct_rltnp (id_cntprty_acct, id_rltd_cntprty_acct, id_orgtn_role, st_cntprty_acct_rltnp) " +
                           "VALUES (:idCntprty, :idRltd, :idRole, 'ACTIVE')";
        return entityManager.createNativeQuery(insertSql)
                .setParameter("idCntprty", idCntprtyAcct)
                .setParameter("idRltd", idRltdCntprtyAcct)
                .setParameter("idRole", idOrgtnRole)
                .executeUpdate();
    }

    @Transactional
    public int insertUcsOrgtnCntct(Object idOrgtnCntct, Object orgId, Object idIndvl) {
        return entityManager.createNativeQuery(
            "INSERT INTO UCS_ORGTN_CNTCT (ID_ORGTN_CNTCT, ID_ORGTN, ID_INDVL) VALUES (:idOrgtnCntct, :idOrgtn, :idIndvl)")
            .setParameter("idOrgtnCntct", idOrgtnCntct)
            .setParameter("idOrgtn", orgId)
            .setParameter("idIndvl", idIndvl)
            .executeUpdate();
    }

    /**
     * JPA Criteria API query construction
     * SQL is generated from Criteria objects (type-safe query builder style).
     */
    @Transactional(readOnly = true)
    public Long getDistinctCounterpartyAccountCount() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<UcsCntprtyAcct> root = countQuery.from(UcsCntprtyAcct.class);
        countQuery.select(cb.countDistinct(root));
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    /**
     * Named JPQL query declaration on entities (Predefined query at entity level).
     */
    @Transactional(readOnly = true)
    public List<UcsAcsGrp> findAllAccessGroupsNamedQuery() {
        return entityManager.createNamedQuery("UcsAcsGrp.findAll", UcsAcsGrp.class).getResultList();
    }
}
