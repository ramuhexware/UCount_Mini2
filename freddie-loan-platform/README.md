# 🏦 Freddie Mac Home Loan Platform (`freddie-loan-platform`)

Welcome to the **Freddie Mac Home Loan Platform** — a high-performance enterprise mortgage application built with **Java 17**, **Spring Boot 3.1.3**, **PostgreSQL**, and **Angular 15**, structured across **10 Java Files** in the backend and **10 Angular Files** in the frontend, maintaining core architectural design patterns, tier separation, and functional business flows.

---

## 🏛️ Streamlined Architecture & File Structure

The platform is streamlined into a single core microservice (`loan-origination-service`) and a modern Angular SPA frontend.

### ☕ 10 Backend Java Files (`loan-origination-service`)
1. [LoanOriginationApplication.java](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/loan-origination-service/src/main/java/com/freddieapp/origination/LoanOriginationApplication.java) — Main Spring Boot Application Entry Point.
2. [LoanConfig.java](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/loan-origination-service/src/main/java/com/freddieapp/origination/config/LoanConfig.java) — Consolidated Security, WebClient, Async, and CORS Configuration.
3. [LoanOriginationController.java](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/loan-origination-service/src/main/java/com/freddieapp/origination/controller/LoanOriginationController.java) — Main REST Controller for Auth, Loans, Accounts, Counterparties, and Underwriting.
4. [AuthController.java](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/loan-origination-service/src/main/java/com/freddieapp/origination/auth/AuthController.java) — Security Authentication Controller for Bearer JWT Tokens.
5. [LoanOriginationService.java](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/loan-origination-service/src/main/java/com/freddieapp/origination/service/LoanOriginationService.java) — Core Business Logic Layer for origination, intake, lookups, and underwriting risk scoring.
6. [LoanApplicationEntity.java](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/loan-origination-service/src/main/java/com/freddieapp/origination/domain/LoanApplicationEntity.java) — JPA Entity for PostgreSQL table `loan_applications`.
7. [LoanApplicationRepository.java](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/loan-origination-service/src/main/java/com/freddieapp/origination/repository/LoanApplicationRepository.java) — Spring Data JPA Repository with PostgreSQL native SQL `@Query`.
8. [LoanDTOs.java](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/loan-origination-service/src/main/java/com/freddieapp/origination/dto/LoanDTOs.java) — Consolidated Record/Class DTOs, Enums, and Exception classes.
9. [UnderwritingRuleProcessor.java](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/loan-origination-service/src/main/java/com/freddieapp/origination/processor/UnderwritingRuleProcessor.java) — Java 17 Switch pattern risk engine and 360-month EMI rate math calculator.
10. [LoanSummaryPdfExporter.java](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/loan-origination-service/src/main/java/com/freddieapp/origination/pdf/LoanSummaryPdfExporter.java) — Loan Summary PDF document exporter.

### 🅰️ 10 Frontend Angular Files (`frontend/src`)
1. [index.html](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/frontend/src/index.html) — HTML Host Entry.
2. [main.ts](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/frontend/src/main.ts) — Main TypeScript Bootstrap.
3. [styles.css](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/frontend/src/styles.css) — Global Application Styling & Glassmorphism Theme.
4. [app/app.config.ts](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/frontend/src/app/app.config.ts) — Angular Application Providers & Config.
5. [app/app.routes.ts](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/frontend/src/app/app.routes.ts) — Router Definitions.
6. [app/app.component.ts](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/frontend/src/app/app.component.ts) — Primary Dashboard Component Logic.
7. [app/app.component.html](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/frontend/src/app/app.component.html) — Master HTML Layout Template.
8. [app/app.component.css](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/frontend/src/app/app.component.css) — Component-level CSS.
9. [app/loan.service.ts](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/frontend/src/app/loan.service.ts) — Unified RxJS HTTP Data Service for Loans, Auth, and Underwriting.
10. [app/auth.interceptor.ts](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/frontend/src/app/auth.interceptor.ts) — HTTP Interceptor for Bearer JWT Authorization headers.

---

## 🔄 6 Core Functional Business Flows

The platform supports **6 streamlined core execution flows**:

1. **Flow 1: Security & OAuth Bearer JWT Issuance**
   - **Endpoint**: `POST /api/v1/auth/login`
   - **Description**: Authenticates users and returns signed Bearer JWT tokens.

2. **Flow 2: Stage 1 & 2 Counterparty Intake & Role Access Control**
   - **Endpoints**: `POST /api/v1/counterparty/stage1/onboard`, `POST /api/v1/counterparty/stage2/profile`
   - **Description**: Handles partner onboarding and evaluates access permissions using Java 17 Switch pattern (`HOUSE_BUYER`, `HOUSE_SELLER`, `MORTGAGE_SERVICER`).

3. **Flow 3: Account Lookup & Profile Management**
   - **Endpoints**: `GET /api/v1/account/lookup/update`, `POST /api/v1/account/create`
   - **Description**: Manages counterparty product catalog lookups and account registration.

4. **Flow 4: Counterparty Relationship & Expiration Tracking**
   - **Service Logic**: `expireAccountRelationship()`
   - **Description**: Detects Seller-Ctos Servicer relationship (ID 25), logs compliance events, and expires cash/mortgage relationships.

5. **Flow 5: Loan Application Origination & PostgreSQL Native SQL State Transition**
   - **Endpoints**: `POST /api/v1/loans`, `POST /api/v1/loans/{id}/submit-underwriting`
   - **Description**: Persists loan application entities in PostgreSQL and transitions status to `UNDER_REVIEW` using high-performance native SQL `@Query`.

6. **Flow 6: Automated Underwriting Risk Engine & Pricing Calculator**
   - **Endpoints**: `POST /api/v1/loans/{id}/submit-underwriting`, `GET /api/v1/rates/quote`, `GET /api/v1/rates/amortization`
   - **Description**: Evaluates DTI & LTV risk scores via Java 17 Switch pattern (`APPROVED`, `REFERRED`, `DECLINED`), calculates credit tier pricing adjustments (`PRIME` to `SUBPRIME`), and generates 360-month EMI amortization schedules.
