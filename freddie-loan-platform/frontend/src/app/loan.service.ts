import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';

export interface User {
  id: string;
  username: string;
  name: string;
  role: 'BORROWER' | 'UNDERWRITER' | 'LOAN_OFFICER' | 'ADMIN';
  email: string;
  token?: string;
}

export interface LoanApplication {
  loanId: string;
  id?: number;
  customerId: string;
  customerName: string;
  email?: string;
  loanType: string;
  loanAmount: number;
  propertyValue: number;
  annualIncome: number;
  monthlyDebt: number;
  ltvRatio: number;
  dtiRatio: number;
  creditScore: number;
  status: 'SUBMITTED' | 'UNDER_REVIEW' | 'APPROVED' | 'CONDITIONAL_APPROVAL' | 'REJECTED' | 'DISBURSED';
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  decisionReason?: string;
  submittedAt: Date;
  documents: { documentId: string; fileName: string; type: string; status: string }[];
}

export interface AssessmentResultDTO {
  loanId: number;
  decision: 'APPROVED' | 'REFERRED' | 'DECLINED';
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  dtiRatio: number;
  ltvRatio: number;
  approvedRate: number;
  monthlyPayment: number;
}

export interface PricingQuoteDTO {
  creditScore: number;
  ltvRatio: number;
  creditTier: string;
  baseRate: number;
  finalRate: number;
  monthlyEMI: number;
}

export interface AmortizationScheduleDTO {
  loanAmount: number;
  interestRate: number;
  termMonths: number;
  monthlyPayment: number;
  schedule: {
    month: number;
    principalPaid: number;
    interestPaid: number;
    remainingBalance: number;
  }[];
}

export interface AccountLookupUpdateDTO {
  ucsLineOfBusinessDTOs: { idLiOfBus: number; nameLiOfBus: string }[];
  ucsCntprtyAcctSt: string[];
  ucsCntprtyAcctRoleSt: string[];
}

export interface TextBlock {
  code: string;
  title: string;
  category: string;
  content: string;
  usageDescription: string;
}

@Injectable({
  providedIn: 'root'
})
export class LoanService {
  private useRealBackend = true;
  private backendBase = '/api/v1';

  private mockUser: User = {
    id: 'USR-8801',
    username: 'mortgage.officer',
    name: 'Sarah Jenkins (Senior Underwriter)',
    role: 'LOAN_OFFICER',
    email: 'sarah.jenkins@freddiemac.com',
    token: 'mock_bearer_jwt_token_8801'
  };

  private currentUserSubject = new BehaviorSubject<User | null>(this.mockUser);
  public currentUser$ = this.currentUserSubject.asObservable();

  private mockApplications: LoanApplication[] = [
    {
      loanId: 'LN-1082',
      id: 1082,
      customerId: 'CUST-802',
      customerName: 'Marcus Vance',
      email: 'marcus@freddiemac.com',
      loanType: 'Purchase (Fixed 30Y)',
      loanAmount: 380000,
      propertyValue: 450000,
      annualIncome: 120000,
      monthlyDebt: 2200,
      ltvRatio: 84.44,
      dtiRatio: 22.00,
      creditScore: 780,
      status: 'APPROVED',
      riskLevel: 'LOW',
      decisionReason: 'Eligible for primary residence standard purchase program.',
      submittedAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000),
      documents: [
        { documentId: 'DOC-1', fileName: 'W2_2025.pdf', type: 'INCOME_PROOF', status: 'VERIFIED' }
      ]
    },
    {
      loanId: 'LN-2940',
      id: 2940,
      customerId: 'CUST-390',
      customerName: 'Sarah Jenkins',
      email: 'sarah@freddiemac.com',
      loanType: 'Refinance (Floating 15Y)',
      loanAmount: 290000,
      propertyValue: 310000,
      annualIncome: 85000,
      monthlyDebt: 3100,
      ltvRatio: 93.55,
      dtiRatio: 43.76,
      creditScore: 650,
      status: 'UNDER_REVIEW',
      riskLevel: 'HIGH',
      decisionReason: 'Elevated DTI ratio and low equity margin. Referred to manual underwriting.',
      submittedAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000),
      documents: []
    }
  ];

  private applicationsSubject = new BehaviorSubject<LoanApplication[]>(this.mockApplications);

  constructor(private http: HttpClient) {}

  // Auth Methods
  login(username: string, role: User['role'] = 'LOAN_OFFICER'): Observable<User> {
    return this.http.post<any>(`${this.backendBase}/auth/login`, { username, password: 'password123' }).pipe(
      map(res => {
        const user: User = {
          id: 'USR-' + Math.floor(1000 + Math.random() * 9000),
          username: res.username || username,
          name: username.replace('.', ' ').toUpperCase(),
          role: role,
          email: `${username}@freddiemac.com`,
          token: res.token || 'mock_bearer_token'
        };
        localStorage.setItem('auth_token', user.token || '');
        this.currentUserSubject.next(user);
        return user;
      }),
      catchError(() => {
        this.currentUserSubject.next(this.mockUser);
        return of(this.mockUser);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('auth_token');
    this.currentUserSubject.next(null);
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  // Loan Methods
  getApplications(): Observable<LoanApplication[]> {
    if (this.useRealBackend) {
      this.http.get<any[]>(`${this.backendBase}/loans`).pipe(
        map(backendApps => backendApps.map(app => this.mapToFrontendModel(app))),
        catchError(err => of(this.mockApplications))
      ).subscribe(apps => {
        if (apps && apps.length > 0) {
          this.applicationsSubject.next(apps);
        }
      });
    }
    return this.applicationsSubject.asObservable();
  }

  submitApplication(appData: Omit<LoanApplication, 'loanId' | 'ltvRatio' | 'dtiRatio' | 'status' | 'riskLevel' | 'submittedAt' | 'documents'>): Observable<LoanApplication> {
    const monthlyIncome = appData.annualIncome / 12;
    const payload = {
      customerId: appData.customerId,
      applicantName: appData.customerName,
      email: `${appData.customerId.toLowerCase()}@freddiemac.com`,
      loanAmount: appData.loanAmount,
      propertyValue: appData.propertyValue,
      monthlyIncome: monthlyIncome,
      monthlyDebt: appData.monthlyDebt,
      creditScore: appData.creditScore,
      termMonths: 360
    };

    return this.http.post<any>(`${this.backendBase}/loans`, payload).pipe(
      map(res => this.mapToFrontendModel(res)),
      tap(newApp => {
        const currentList = this.applicationsSubject.value;
        this.applicationsSubject.next([newApp, ...currentList]);
      }),
      catchError(() => this.fallbackLocalSubmit(appData))
    );
  }

  submitForUnderwritingNative(numericLoanId: number): Observable<AssessmentResultDTO> {
    return this.http.post<AssessmentResultDTO>(`${this.backendBase}/loans/${numericLoanId}/submit-underwriting`, {});
  }

  getPricingQuote(creditScore: number = 720, ltvRatio: number = 80.0): Observable<PricingQuoteDTO> {
    return this.http.get<PricingQuoteDTO>(`${this.backendBase}/rates/quote?creditScore=${creditScore}&ltvRatio=${ltvRatio}`);
  }

  getAmortizationSchedule(loanAmount: number = 350000, interestRate: number = 6.50, termMonths: number = 360): Observable<AmortizationScheduleDTO> {
    return this.http.get<AmortizationScheduleDTO>(`${this.backendBase}/rates/amortization?loanAmount=${loanAmount}&interestRate=${interestRate}&termMonths=${termMonths}`);
  }

  getAccountLookupUpdate(): Observable<AccountLookupUpdateDTO> {
    return this.http.get<AccountLookupUpdateDTO>(`${this.backendBase}/account/lookup/update`);
  }

  createAccount(accountReq: any): Observable<any> {
    return this.http.post<any>(`${this.backendBase}/account/create`, accountReq);
  }

  onboardStage1User(req: { orgName: string; email: string }): Observable<any> {
    return this.http.post<any>(`${this.backendBase}/counterparty/stage1/onboard`, req);
  }

  saveStage2Profile(req: { userId: string; userType: string }): Observable<any> {
    return this.http.post<any>(`${this.backendBase}/counterparty/stage2/profile`, req);
  }

  private fallbackLocalSubmit(appData: any): Observable<LoanApplication> {
    const ltv = Number(((appData.loanAmount / appData.propertyValue) * 100).toFixed(2));
    const dti = Number(((appData.monthlyDebt / (appData.annualIncome / 12)) * 100).toFixed(2));
    const newApp: LoanApplication = {
      ...appData,
      loanId: 'LN-' + Math.floor(1000 + Math.random() * 9000),
      ltvRatio: ltv,
      dtiRatio: dti,
      status: 'SUBMITTED',
      riskLevel: 'LOW',
      decisionReason: 'Intake process complete.',
      submittedAt: new Date(),
      documents: []
    };
    const currentList = this.applicationsSubject.value;
    this.applicationsSubject.next([newApp, ...currentList]);
    return of(newApp);
  }

  private mapToFrontendModel(res: any): LoanApplication {
    const rawId = res.loanId || res.id;
    return {
      loanId: rawId ? `LN-${rawId}` : 'LN-' + Math.floor(1000 + Math.random() * 9000),
      id: rawId,
      customerId: res.customerId || 'CUST-TBD',
      customerName: res.applicantName || res.customerName || 'Mortgage Client',
      email: res.email,
      loanType: 'Purchase (Fixed 30Y)',
      loanAmount: res.loanAmount || 0,
      propertyValue: res.propertyValue || 0,
      annualIncome: res.monthlyIncome ? res.monthlyIncome * 12 : 95000,
      monthlyDebt: res.monthlyDebt || 1200,
      ltvRatio: res.ltvRatio || 80.0,
      dtiRatio: res.dtiRatio || 25.0,
      creditScore: res.creditScore || 720,
      status: res.status || 'SUBMITTED',
      riskLevel: res.riskLevel || 'LOW',
      decisionReason: res.decisionReason || 'Intake process complete.',
      submittedAt: res.createdAt ? new Date(res.createdAt) : new Date(),
      documents: res.documents || []
    };
  }

  getTextBlocks(): Observable<TextBlock[]> {
    return this.http.get<TextBlock[]>(`${this.backendBase}/text-blocks`).pipe(
      catchError(() => of([
        {
          code: 'TILA_DISCLOSURE',
          title: 'Truth in Lending Act (TILA) Initial Disclosure',
          category: 'COMPLIANCE_LEGAL',
          content: `FEDERAL TRUTH IN LENDING DISCLOSURE STATEMENT
====================================================================
1. ANNUAL PERCENTAGE RATE (APR): The cost of your credit as a yearly rate.
2. FINANCE CHARGE: The dollar amount the credit will cost you.
3. AMOUNT FINANCED: The amount of credit provided to you or on your behalf.
4. TOTAL OF PAYMENTS: The amount you will have paid after making all scheduled payments.
--------------------------------------------------------------------
Notice: You are not required to complete this agreement merely because you have
received these disclosures or signed an application.
====================================================================`,
          usageDescription: 'Standard federal TILA disclosure provided to mortgage applicants upon origination.'
        },
        {
          code: 'ECOA_NOTICE',
          title: 'Equal Credit Opportunity Act (ECOA) Notice',
          category: 'REGULATORY_RIGHTS',
          content: `EQUAL CREDIT OPPORTUNITY ACT NOTICE
====================================================================
The Federal Equal Credit Opportunity Act prohibits creditors from discriminating
against credit applicants on the basis of race, color, religion, national origin,
sex, marital status, age, or because all or part of the applicant's income derives
from any public assistance program.
--------------------------------------------------------------------
The Federal Agency that administers compliance with this law concerning this creditor is:
Consumer Financial Protection Bureau (CFPB), 1700 G Street NW, Washington, DC 20006.
====================================================================`,
          usageDescription: 'Mandatory fair lending disclosure document presented to all loan originators and buyers.'
        },
        {
          code: 'UNDERWRITING_NOTES',
          title: 'Automated Underwriting System (AUS) Decision Notes',
          category: 'UNDERWRITING_RULES',
          content: `FREDDIE MAC AUTOMATED UNDERWRITING EVALUATION SUMMARY
====================================================================
Criteria Checked:
  - Debt-To-Income (DTI) Ratio  : Threshold <= 45.0%
  - Loan-To-Value (LTV) Ratio  : Threshold <= 80.0% (No PMI required)
  - FICO Credit Risk Tier      : Prime (750+), Near-Prime (650-749)
--------------------------------------------------------------------
Evaluation Rule Pattern: Java 17 Switch Pattern Risk Engine
Primary Market Eligibility : Eligible for Single-Family Purchased Mortgages
====================================================================`,
          usageDescription: 'Multi-line summary block of underwriting decision rules applied during evaluation.'
        },
        {
          code: 'POSTGRES_SQL_BLOCK',
          title: 'Native PostgreSQL State Transition Text Block',
          category: 'SYSTEM_DATABASE',
          content: `UPDATE loan_applications
SET status = :status,
    updated_at = NOW()
WHERE id = :loanId
  AND status != 'ARCHIVED';`,
          usageDescription: 'Native PostgreSQL multi-line SQL statement used in JPA LoanApplicationRepository.'
        },
        {
          code: 'PDF_TEMPLATE_BLOCK',
          title: 'Loan Summary PDF Exporter Document Structure',
          category: 'DOCUMENT_EXPORTER',
          content: `%PDF-1.4
------------------------------------------------------------------
FREDDIE MAC HOME LOAN PLATFORM - MORTGAGE SUMMARY DISCLOSURE
------------------------------------------------------------------
Application ID : LN-%d
Borrower Name  : %s
Loan Amount    : $%s
App Status     : %s
------------------------------------------------------------------
EQUAL HOUSING OPPORTUNITY - FREDDIE MAC ENTERPRISE`,
          usageDescription: 'Java 17 text block format string template for binary PDF stream generation.'
        }
      ]))
    );
  }
}
