package com.freddieapp.origination;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for Loan Origination Microservice (Port 8082).
 */
@SpringBootApplication
public class LoanOriginationApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoanOriginationApplication.class, args);
        String startupBanner = """
            ====================================================================
            FREDDIE MAC HOME LOAN PLATFORM - LOAN ORIGINATION SERVICE (PORT 8082)
            ====================================================================
            Java Version : 17
            Features     : Native PostgreSQL Queries, Underwriting Switch Rules,
                           Java 17 Text Blocks Multiline Disclosures & Reports
            ====================================================================
            """;
        System.out.println(startupBanner);
    }
}
