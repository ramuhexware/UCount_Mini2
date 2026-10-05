package com.freddieapp.origination.controller;

import com.freddieapp.origination.domain.UcsAcsGrp;
import com.freddieapp.origination.dto.LoanDTOs.*;
import com.freddieapp.origination.pdf.LoanSummaryPdfExporter;
import com.freddieapp.origination.service.LoanOriginationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class LoanOriginationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoanOriginationController.class);
    private final LoanOriginationService service;
    private final LoanSummaryPdfExporter pdfExporter;

    @Autowired
    public LoanOriginationController(LoanOriginationService service, LoanSummaryPdfExporter pdfExporter) {
        this.service = service;
        this.pdfExporter = pdfExporter;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("id", "status", "createdAt");
    }

    // Flow 5: Loan Origination
    @PostMapping("/loans")
    public ResponseEntity<LoanResponseDTO> createLoan(@RequestBody LoanRequestDTO request) {
        LOGGER.info("REST: Creating loan application for customer: {}", request.customerId());
        return new ResponseEntity<>(service.createLoanApplication(request), HttpStatus.CREATED);
    }

    @GetMapping("/loans/{id}")
    public ResponseEntity<LoanResponseDTO> getLoanById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getLoanById(id));
    }

    @GetMapping(value = "/loans/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadLoanSummaryPdf(@PathVariable Long id) {
        LoanResponseDTO loan = service.getLoanById(id);
        byte[] pdfBytes = pdfExporter.generateLoanSummaryPdf(loan);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Loan_Summary_" + id + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdfBytes);
    }

    @GetMapping("/loans/customer/{customerId}")
    public ResponseEntity<List<LoanResponseDTO>> getLoansByCustomer(@PathVariable String customerId) {
        return ResponseEntity.ok(service.getLoansByCustomerId(customerId));
    }

    // Flow 6: Underwriting Assessment & Native SQL State Transition
    @PostMapping("/loans/{id}/submit-underwriting")
    public ResponseEntity<AssessmentResultDTO> submitForUnderwritingNative(@PathVariable Long id) {
        LOGGER.info("REST: Underwriting risk assessment trigger for loan {}", id);
        return ResponseEntity.ok(service.submitForUnderwritingNative(id));
    }

    @GetMapping("/rates/quote")
    public ResponseEntity<PricingQuoteDTO> getPricingQuote(
            @RequestParam(defaultValue = "720") Integer creditScore,
            @RequestParam(defaultValue = "80.0") BigDecimal ltvRatio) {
        return ResponseEntity.ok(service.getPricingQuote(creditScore, ltvRatio));
    }

    @GetMapping("/rates/amortization")
    public ResponseEntity<AmortizationScheduleDTO> getAmortizationSchedule(
            @RequestParam(defaultValue = "350000") BigDecimal loanAmount,
            @RequestParam(defaultValue = "6.50") BigDecimal interestRate,
            @RequestParam(defaultValue = "360") Integer termMonths) {
        return ResponseEntity.ok(service.getAmortizationSchedule(loanAmount, interestRate, termMonths));
    }

    // Flow 3: Account Lookups & Profile Management
    @GetMapping("/account/lookup")
    public ResponseEntity<AccountLookupDTO> getLookupData() {
        return ResponseEntity.ok(service.getLookupData());
    }

    @GetMapping("/account/lookup/update")
    public ResponseEntity<AccountLookupUpdateDTO> getLookupUpdateData() {
        return ResponseEntity.ok(service.getLookupUpdateData());
    }

    @PostMapping("/account/create")
    public ResponseEntity<AccountSaveDTO> createAccount(@RequestBody AccountSaveDTO accountSaveDTO) {
        return ResponseEntity.ok(service.createAccount(accountSaveDTO));
    }

    @PostMapping("/account/profile")
    public ResponseEntity<AccountProfileRespDTO> getAccountProfile(@RequestBody AccountProfileReqDTO req) {
        return ResponseEntity.ok(service.getAccountProfile(req));
    }

    // Flow 2: Stage 1 & 2 Counterparty Intake
    @PostMapping("/counterparty/stage1/onboard")
    public ResponseEntity<Stage1UserResponseDTO> onboardStage1User(@RequestBody Stage1OnboardRequestDTO req) {
        return new ResponseEntity<>(service.onboardStage1User(req), HttpStatus.CREATED);
    }

    @PostMapping("/counterparty/stage1/approve/{userId}")
    public ResponseEntity<Stage1UserResponseDTO> approveStage1User(@PathVariable String userId) {
        return ResponseEntity.ok(service.approveStage1User(userId));
    }

    @GetMapping("/counterparty/stage1/pending")
    public ResponseEntity<List<Stage1UserResponseDTO>> getPendingStage1Users() {
        return ResponseEntity.ok(service.getPendingStage1Users());
    }

    @PostMapping("/counterparty/stage2/profile")
    public ResponseEntity<Stage2AccessRightsResponseDTO> saveStage2Profile(@RequestBody Stage2ProfileRequestDTO req) {
        return ResponseEntity.ok(service.saveStage2Profile(req));
    }

    @GetMapping("/counterparty/stage2/access-rights/{userId}")
    public ResponseEntity<Stage2AccessRightsResponseDTO> getStage2AccessRights(@PathVariable String userId) {
        return ResponseEntity.ok(service.getStage2AccessRights(userId));
    }

    // Java 17 Text Blocks API Endpoints
    @GetMapping("/text-blocks")
    public ResponseEntity<List<TextBlockDTO>> getTextBlocks() {
        return ResponseEntity.ok(service.getTextBlocks());
    }

    @GetMapping("/text-blocks/{code}")
    public ResponseEntity<TextBlockDTO> getTextBlockByCode(@PathVariable String code) {
        return ResponseEntity.ok(service.getTextBlockByCode(code));
    }

    // Dynamic Native Query Endpoint through EntityManager.createNativeQuery
    @GetMapping("/counterparty/relationships/search")
    public ResponseEntity<List<Object[]>> searchCounterpartyRelationships(@RequestParam(required = false) String searchCriteria) {
        return ResponseEntity.ok(service.searchCounterpartyRelationships(searchCriteria));
    }

    // JPA Criteria API Query Construction Endpoint
    @GetMapping("/counterparty/accounts/count")
    public ResponseEntity<Long> getDistinctCounterpartyAccountCount() {
        return ResponseEntity.ok(service.getDistinctCounterpartyAccountCount());
    }

    // Named JPQL Query Declaration Endpoint
    @GetMapping("/access-groups")
    public ResponseEntity<List<UcsAcsGrp>> findAllAccessGroups() {
        return ResponseEntity.ok(service.findAllAccessGroupsNamedQuery());
    }

    // Complex Native SQL Query (Multi-table join, Correlated Subquery, CASE WHEN, Derived UNION, Const injection)
    @GetMapping("/reports/complex-summary")
    public ResponseEntity<List<Object[]>> getComplexMultiTableDerivedSummary() {
        return ResponseEntity.ok(service.getComplexMultiTableDerivedSummary());
    }
}
