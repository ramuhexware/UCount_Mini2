package com.freddieapp.origination.pdf;

import com.freddieapp.origination.dto.LoanDTOs.LoanResponseDTO;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

@Component
public class LoanSummaryPdfExporter {

    public byte[] generateLoanSummaryPdf(LoanResponseDTO loan) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String pdfContent = """
            %PDF-1.4
            ------------------------------------------------------------------
            FREDDIE MAC HOME LOAN PLATFORM - MORTGAGE SUMMARY DISCLOSURE
            ------------------------------------------------------------------
            Application ID : LN-%d
            Borrower Name  : %s
            Loan Amount    : $%s
            App Status     : %s
            ------------------------------------------------------------------
            EQUAL HOUSING OPPORTUNITY - FREDDIE MAC ENTERPRISE
            """.formatted(
                loan.id(),
                loan.applicantName(),
                loan.loanAmount(),
                loan.status()
            );
        out.writeBytes(pdfContent.getBytes(StandardCharsets.UTF_8));
        return out.toByteArray();
    }
}
