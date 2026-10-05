package com.freddieapp.origination.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Consolidated DTOs and Enums for Loan Origination & Underwriting System.
 */
public class LoanDTOs {

    public static class UcsApiException extends RuntimeException {
        private final org.springframework.http.HttpStatus status;
        public UcsApiException(org.springframework.http.HttpStatus status, String message) {
            super(message);
            this.status = status;
        }
        public org.springframework.http.HttpStatus getStatus() { return status; }
    }

    // Auth DTOs
    public record LoginRequestDTO(String username, String password) {}
    public record AuthResponseDTO(String token, String tokenType, String username, String role) {}

    // Loan Application DTOs
    public record LoanRequestDTO(
        String customerId,
        String applicantName,
        String email,
        BigDecimal loanAmount,
        BigDecimal propertyValue,
        BigDecimal monthlyIncome,
        BigDecimal monthlyDebt,
        Integer creditScore,
        Integer termMonths
    ) {}

    public record LoanResponseDTO(
        Long id,
        String customerId,
        String applicantName,
        String email,
        BigDecimal loanAmount,
        BigDecimal propertyValue,
        Integer creditScore,
        String status,
        LocalDateTime createdAt
    ) {}

    // Account & Counterparty Lookups
    public record AccountLookupDTO(
        List<String> accountTypes,
        List<String> partnerRoles,
        Map<String, String> systemMetadata
    ) {}

    public static class AccountLookupUpdateDTO {
        private List<UcsLineOfBusinessDTO> ucsLineOfBusinessDTOs;
        private Map<String, List<UcsOrgtnRoleDTO>> ucsOrgtnRoleDTOs;
        private Map<String, List<UcsProdtDTO>> ucsProdtDTOs;
        private List<String> ucsCntprtyAcctSt;
        private List<String> ucsCntprtyAcctRoleSt;
        private ResponseStatusDTO respSts;

        public List<UcsLineOfBusinessDTO> getUcsLineOfBusinessDTOs() { return ucsLineOfBusinessDTOs; }
        public void setUcsLineOfBusinessDTOs(List<UcsLineOfBusinessDTO> ucsLineOfBusinessDTOs) { this.ucsLineOfBusinessDTOs = ucsLineOfBusinessDTOs; }
        public Map<String, List<UcsOrgtnRoleDTO>> getUcsOrgtnRoleDTOs() { return ucsOrgtnRoleDTOs; }
        public void setUcsOrgtnRoleDTOs(Map<String, List<UcsOrgtnRoleDTO>> ucsOrgtnRoleDTOs) { this.ucsOrgtnRoleDTOs = ucsOrgtnRoleDTOs; }
        public Map<String, List<UcsProdtDTO>> getUcsProdtDTOs() { return ucsProdtDTOs; }
        public void setUcsProdtDTOs(Map<String, List<UcsProdtDTO>> ucsProdtDTOs) { this.ucsProdtDTOs = ucsProdtDTOs; }
        public List<String> getUcsCntprtyAcctSt() { return ucsCntprtyAcctSt; }
        public void setUcsCntprtyAcctSt(List<String> ucsCntprtyAcctSt) { this.ucsCntprtyAcctSt = ucsCntprtyAcctSt; }
        public List<String> getUcsCntprtyAcctRoleSt() { return ucsCntprtyAcctRoleSt; }
        public void setUcsCntprtyAcctRoleSt(List<String> ucsCntprtyAcctRoleSt) { this.ucsCntprtyAcctRoleSt = ucsCntprtyAcctRoleSt; }
        public ResponseStatusDTO getRespSts() { return respSts; }
        public void setRespSts(ResponseStatusDTO respSts) { this.respSts = respSts; }
    }

    public static class AccountSaveDTO {
        private String idCntprtyAcct;
        private String orgName;
        private String acctType;
        private ResponseStatusDTO respSts;

        public String getIdCntprtyAcct() { return idCntprtyAcct; }
        public void setIdCntprtyAcct(String idCntprtyAcct) { this.idCntprtyAcct = idCntprtyAcct; }
        public String getOrgName() { return orgName; }
        public void setOrgName(String orgName) { this.orgName = orgName; }
        public String getAcctType() { return acctType; }
        public void setAcctType(String acctType) { this.acctType = acctType; }
        public ResponseStatusDTO getRespSts() { return respSts; }
        public void setRespSts(ResponseStatusDTO respSts) { this.respSts = respSts; }
    }

    public record AccountProfileReqDTO(String idCntprtyAcct) {}
    public record AccountProfileRespDTO(String idCntprtyAcct, String status, List<String> permissions) {}

    public static class ResponseStatusDTO {
        private int statusCode;
        private String message;

        public ResponseStatusDTO() {}
        public ResponseStatusDTO(int statusCode, String message) {
            this.statusCode = statusCode;
            this.message = message;
        }
        public int getStatusCode() { return statusCode; }
        public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    // Stage 1 & Stage 2 Intake DTOs
    public record Stage1OnboardRequestDTO(String orgName, String email, String contactPerson) {}
    public record Stage1UserResponseDTO(String userId, String orgName, String email, Stage1Status status, LocalDateTime createdAt) {}
    public enum Stage1Status { PENDING_APPROVAL, APPROVED, REJECTED }

    public record Stage2ProfileRequestDTO(String userId, UserType userType, String businessTaxId) {}
    public record Stage2AccessRightsResponseDTO(String userId, UserType userType, List<String> accessRights) {}

    public enum UserType {
        HOUSE_SELLER,
        HOUSE_BUYER,
        INSURANCE_PERSON,
        MORTGAGE_SERVICER
    }

    public static class UcsLineOfBusinessDTO {
        private int idLiOfBus;
        private String nameLiOfBus;
        public UcsLineOfBusinessDTO() {}
        public UcsLineOfBusinessDTO(int idLiOfBus, String nameLiOfBus) {
            this.idLiOfBus = idLiOfBus;
            this.nameLiOfBus = nameLiOfBus;
        }
        public int getIdLiOfBus() { return idLiOfBus; }
        public void setIdLiOfBus(int idLiOfBus) { this.idLiOfBus = idLiOfBus; }
        public String getNameLiOfBus() { return nameLiOfBus; }
        public void setNameLiOfBus(String nameLiOfBus) { this.nameLiOfBus = nameLiOfBus; }
    }

    public static class UcsOrgtnRoleDTO {
        private int idOrgtnRole;
        private String nameOrgtnRole;
        public UcsOrgtnRoleDTO() {}
        public UcsOrgtnRoleDTO(int idOrgtnRole, String nameOrgtnRole) {
            this.idOrgtnRole = idOrgtnRole;
            this.nameOrgtnRole = nameOrgtnRole;
        }
        public int getIdOrgtnRole() { return idOrgtnRole; }
        public void setIdOrgtnRole(int idOrgtnRole) { this.idOrgtnRole = idOrgtnRole; }
        public String getNameOrgtnRole() { return nameOrgtnRole; }
        public void setNameOrgtnRole(String nameOrgtnRole) { this.nameOrgtnRole = nameOrgtnRole; }
    }

    public static class UcsProdtDTO {
        private int idProdt;
        private String nameProdt;
        public UcsProdtDTO() {}
        public UcsProdtDTO(int idProdt, String nameProdt) {
            this.idProdt = idProdt;
            this.nameProdt = nameProdt;
        }
        public int getIdProdt() { return idProdt; }
        public void setIdProdt(int idProdt) { this.idProdt = idProdt; }
        public String getNameProdt() { return nameProdt; }
        public void setNameProdt(String nameProdt) { this.nameProdt = nameProdt; }
    }

    // Underwriting & Rates DTOs
    public record AssessmentRequestDTO(
        Long loanId,
        String customerId,
        BigDecimal loanAmount,
        BigDecimal propertyValue,
        BigDecimal monthlyIncome,
        BigDecimal monthlyDebt,
        Integer creditScore,
        Integer termMonths
    ) {}

    public record AssessmentResultDTO(
        Long loanId,
        String decision,
        String riskLevel,
        BigDecimal dtiRatio,
        BigDecimal ltvRatio,
        BigDecimal approvedRate,
        BigDecimal monthlyPayment
    ) {}

    public record PricingQuoteDTO(
        Integer creditScore,
        BigDecimal ltvRatio,
        String creditTier,
        BigDecimal baseRate,
        BigDecimal finalRate,
        BigDecimal monthlyEMI
    ) {}

    public record AmortizationScheduleDTO(
        BigDecimal loanAmount,
        BigDecimal interestRate,
        Integer termMonths,
        BigDecimal monthlyPayment,
        BigDecimal totalInterestPaid,
        List<AmortizationMonthDTO> schedule
    ) {}

    public record AmortizationMonthDTO(
        int month,
        BigDecimal principalPaid,
        BigDecimal interestPaid,
        BigDecimal remainingBalance
    ) {}

    // Text Block Management DTO
    public record TextBlockDTO(
        String code,
        String title,
        String category,
        String content,
        String usageDescription
    ) {}
}
