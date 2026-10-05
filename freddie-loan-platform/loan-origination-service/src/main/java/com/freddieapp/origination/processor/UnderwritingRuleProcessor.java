package com.freddieapp.origination.processor;

import com.freddieapp.origination.dto.LoanDTOs.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Business Rule Processor for Underwriting Risk Scoring & Rate Calculations.
 */
@Component
public class UnderwritingRuleProcessor {

    public AssessmentResultDTO evaluateRisk(AssessmentRequestDTO request) {
        BigDecimal monthlyIncome = (request.monthlyIncome() != null && request.monthlyIncome().compareTo(BigDecimal.ZERO) > 0)
            ? request.monthlyIncome() : BigDecimal.valueOf(8000);
        BigDecimal monthlyDebt = (request.monthlyDebt() != null) ? request.monthlyDebt() : BigDecimal.valueOf(2000);
        BigDecimal propertyValue = (request.propertyValue() != null && request.propertyValue().compareTo(BigDecimal.ZERO) > 0)
            ? request.propertyValue() : request.loanAmount().multiply(BigDecimal.valueOf(1.25));

        // Calculate DTI Ratio = (Monthly Debt / Monthly Income) * 100
        BigDecimal dtiRatio = monthlyDebt.divide(monthlyIncome, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        // Calculate LTV Ratio = (Loan Amount / Property Value) * 100
        BigDecimal ltvRatio = request.loanAmount().divide(propertyValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        Integer creditScore = (request.creditScore() != null) ? request.creditScore() : 720;

        // Java 17 Switch Pattern Rule Engine
        String decision;
        String riskLevel;

        if (creditScore >= 740 && dtiRatio.compareTo(BigDecimal.valueOf(43)) <= 0 && ltvRatio.compareTo(BigDecimal.valueOf(80)) <= 0) {
            decision = "APPROVED";
            riskLevel = "LOW";
        } else if (creditScore >= 660 && dtiRatio.compareTo(BigDecimal.valueOf(50)) <= 0 && ltvRatio.compareTo(BigDecimal.valueOf(90)) <= 0) {
            decision = "REFERRED";
            riskLevel = "MEDIUM";
        } else {
            decision = "DECLINED";
            riskLevel = "HIGH";
        }

        PricingQuoteDTO quote = calculatePricingQuote(creditScore, ltvRatio);

        return new AssessmentResultDTO(
            request.loanId(),
            decision,
            riskLevel,
            dtiRatio.setScale(2, RoundingMode.HALF_UP),
            ltvRatio.setScale(2, RoundingMode.HALF_UP),
            quote.finalRate(),
            quote.monthlyEMI()
        );
    }

    public PricingQuoteDTO calculatePricingQuote(Integer creditScore, BigDecimal ltvRatio) {
        String creditTier = switch (creditScore / 50) {
            case 16, 15 -> "PRIME";       // 750-850
            case 14, 13 -> "NEAR_PRIME";  // 650-749
            case 12, 11 -> "NON_PRIME";   // 550-649
            default -> "SUBPRIME";       // <550
        };

        BigDecimal baseRate = BigDecimal.valueOf(6.25);
        BigDecimal adjustment = switch (creditTier) {
            case "PRIME" -> BigDecimal.valueOf(-0.50);
            case "NEAR_PRIME" -> BigDecimal.ZERO;
            case "NON_PRIME" -> BigDecimal.valueOf(0.75);
            default -> BigDecimal.valueOf(2.00);
        };

        if (ltvRatio != null && ltvRatio.compareTo(BigDecimal.valueOf(80)) > 0) {
            adjustment = adjustment.add(BigDecimal.valueOf(0.25));
        }

        BigDecimal finalRate = baseRate.add(adjustment).setScale(2, RoundingMode.HALF_UP);
        BigDecimal monthlyEMI = calculateEMI(BigDecimal.valueOf(350000), finalRate, 360);

        return new PricingQuoteDTO(creditScore, ltvRatio, creditTier, baseRate, finalRate, monthlyEMI);
    }

    public AmortizationScheduleDTO generateAmortizationSchedule(BigDecimal loanAmount, BigDecimal annualRate, int termMonths) {
        BigDecimal monthlyPayment = calculateEMI(loanAmount, annualRate, termMonths);
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);

        BigDecimal remainingBalance = loanAmount;
        BigDecimal totalInterestPaid = BigDecimal.ZERO;
        List<AmortizationMonthDTO> schedule = new ArrayList<>();

        for (int month = 1; month <= Math.min(termMonths, 12); month++) {
            BigDecimal interestPaid = remainingBalance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPaid = monthlyPayment.subtract(interestPaid).setScale(2, RoundingMode.HALF_UP);
            remainingBalance = remainingBalance.subtract(principalPaid).setScale(2, RoundingMode.HALF_UP);
            if (remainingBalance.compareTo(BigDecimal.ZERO) < 0) remainingBalance = BigDecimal.ZERO;
            totalInterestPaid = totalInterestPaid.add(interestPaid);

            schedule.add(new AmortizationMonthDTO(month, principalPaid, interestPaid, remainingBalance));
        }

        return new AmortizationScheduleDTO(loanAmount, annualRate, termMonths, monthlyPayment, totalInterestPaid, schedule);
    }

    private BigDecimal calculateEMI(BigDecimal loanAmount, BigDecimal annualRate, int termMonths) {
        double r = annualRate.doubleValue() / 1200.0;
        double n = termMonths;
        double p = loanAmount.doubleValue();

        if (r == 0) return BigDecimal.valueOf(p / n).setScale(2, RoundingMode.HALF_UP);

        double emi = (p * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
        return BigDecimal.valueOf(emi).setScale(2, RoundingMode.HALF_UP);
    }

    public String generateUnderwritingSummaryTextBlock(AssessmentResultDTO result) {
        return """
            ================================================================
            FREDDIE MAC UNDERWRITING DECISION SUMMARY REPORT
            ================================================================
            Loan ID              : %d
            Decision Output      : %s
            Risk Tier Level      : %s
            Calculated DTI Ratio : %s%%
            Calculated LTV Ratio : %s%%
            Approved Rate Quote  : %s%%
            Monthly EMI Payment  : $%s
            ----------------------------------------------------------------
            Audited Rule Engine  : Java 17 Switch Pattern Underwriting Engine
            ================================================================
            """.formatted(
                result.loanId(),
                result.decision(),
                result.riskLevel(),
                result.dtiRatio(),
                result.ltvRatio(),
                result.approvedRate(),
                result.monthlyPayment()
            );
    }
}
