import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LoanService, LoanApplication, PricingQuoteDTO, AmortizationScheduleDTO, AccountLookupUpdateDTO, TextBlock } from './loan.service';

interface AuditLog {
  timestamp: Date;
  user: string;
  action: string;
  details: string;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'Freddie Mac Mortgage Portal';
  
  // Login & Session State
  isLoggedIn = false;
  isLoggingIn = false;
  loginEmail = 'borrower@freddiemac.com';
  loginPassword = '••••••••';
  selectedLoginRole: 'Borrower' | 'LoanOfficer' | 'Underwriter' | 'OpsManager' | 'Compliance' = 'Borrower';
  loginError = '';

  // Navigation & Role State
  activeRole: 'Borrower' | 'LoanOfficer' | 'Underwriter' | 'OpsManager' | 'Compliance' = 'Borrower';
  activeTab: 'dashboard' | 'origination' | 'underwriting' | 'documents' | 'servicing' | 'compliance' | 'borrowerStatus' | 'textBlocks' = 'dashboard';
  
  // Data State
  applications: LoanApplication[] = [];
  selectedApp: LoanApplication | null = null;
  
  // Text Blocks State
  textBlocks: TextBlock[] = [];
  selectedTextBlock: TextBlock | null = null;
  copiedBlockCode: string | null = null;

  // Origination Form Model
  formCustomerName = '';
  formCustomerId = 'CUST-' + Math.floor(100 + Math.random() * 900);
  formLoanType = 'Purchase (Fixed 30Y)';
  formLoanAmount = 250000;
  formPropertyValue = 300000;
  formAnnualIncome = 95000;
  formMonthlyDebt = 1200;
  formCreditScore = 720;
  
  // Underwriter Controls & Rate Calculator State
  overrideDecision: 'APPROVED' | 'REJECTED' = 'APPROVED';
  overrideReason = '';
  pricingQuoteResult: PricingQuoteDTO | null = null;
  amortizationScheduleResult: AmortizationScheduleDTO | null = null;
  batchJobResult: any = null;
  accountLookupResult: AccountLookupUpdateDTO | null = null;
  
  // Document Uploads
  uploadDocType = 'INCOME_PROOF';
  selectedFile: File | null = null;
  myActiveApp: LoanApplication | null = null;
  
  // Audits logs
  auditLogs: AuditLog[] = [
    { timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000), user: 'SYSTEM_GATEWAY', action: 'TOKEN_VALIDATION', details: 'OAuth2 JWT token validated successfully for User: LOAN_OFFICER_01' }
  ];

  constructor(private loanService: LoanService) {}

  ngOnInit() {
    this.loanService.getApplications().subscribe(apps => {
      this.applications = apps;
      if (apps.length > 0) {
        if (!this.selectedApp) {
          this.selectedApp = apps[0];
        } else {
          const updated = apps.find(a => a.loanId === this.selectedApp?.loanId);
          if (updated) {
            this.selectedApp = updated;
          }
        }
      }
    });

    this.fetchPricingQuote();
    this.fetchTextBlocks();
  }

  get borrowerActiveApp(): LoanApplication | null {
    if (this.myActiveApp) return this.myActiveApp;
    if (this.applications.length > 0) return this.applications[0];
    return null;
  }

  selectLoginRole(role: typeof this.selectedLoginRole) {
    this.selectedLoginRole = role;
    if (role === 'Borrower') {
      this.loginEmail = 'borrower@freddiemac.com';
    } else if (role === 'Underwriter') {
      this.loginEmail = 'underwriter@freddiemac.com';
    } else {
      this.loginEmail = 'officer@freddiemac.com';
    }
  }

  handleLogin() {
    this.isLoggingIn = true;
    const username = this.selectedLoginRole === 'Borrower' ? 'customer' : 'officer';
    this.loanService.login(username).subscribe({
      next: () => {
        this.isLoggingIn = false;
        this.isLoggedIn = true;
        this.activeRole = this.selectedLoginRole;
        this.addAuditLog('USER_LOGIN', `Logged in successfully as ${this.activeRole}`);
        if (this.activeRole === 'Borrower') this.activeTab = 'origination';
        else if (this.activeRole === 'Underwriter') this.activeTab = 'underwriting';
        else this.activeTab = 'dashboard';
      },
      error: () => {
        this.isLoggingIn = false;
        this.loginError = 'Authentication failed.';
      }
    });
  }

  handleLogout() {
    this.loanService.logout();
    this.isLoggedIn = false;
    this.selectLoginRole('Borrower');
  }

  selectRole(role: typeof this.activeRole) {
    this.activeRole = role;
    if (role === 'Underwriter') this.activeTab = 'underwriting';
    else this.activeTab = 'dashboard';
  }

  selectTab(tab: typeof this.activeTab) {
    this.activeTab = tab;
  }

  selectApp(app: LoanApplication) {
    this.selectedApp = app;
  }

  get computedLtv(): number {
    if (!this.formPropertyValue) return 0;
    return Number(((this.formLoanAmount / this.formPropertyValue) * 100).toFixed(2));
  }

  get computedDti(): number {
    const monthlyIncome = this.formAnnualIncome / 12;
    if (!monthlyIncome) return 0;
    return Number(((this.formMonthlyDebt / monthlyIncome) * 100).toFixed(2));
  }

  submitApplication() {
    if (!this.formCustomerName) {
      alert('Please provide borrower name.');
      return;
    }

    const payload = {
      customerId: this.formCustomerId,
      customerName: this.formCustomerName,
      loanType: this.formLoanType,
      loanAmount: this.formLoanAmount,
      propertyValue: this.formPropertyValue,
      annualIncome: this.formAnnualIncome,
      monthlyDebt: this.formMonthlyDebt,
      creditScore: this.formCreditScore
    };

    this.loanService.submitApplication(payload).subscribe(newApp => {
      this.addAuditLog('LOAN_SUBMITTED', `Created loan application ${newApp.loanId} for ${newApp.customerName}`);
      this.selectedApp = newApp;
      this.myActiveApp = newApp;
      this.activeTab = 'dashboard';
      this.formCustomerName = '';
      this.formCustomerId = 'CUST-' + Math.floor(100 + Math.random() * 900);
    });
  }

  triggerUnderwriting(app: LoanApplication) {
    const numericId = app.id || parseInt(app.loanId.replace('LN-', ''), 10);
    if (numericId) {
      this.loanService.submitForUnderwritingNative(numericId).subscribe(result => {
        this.addAuditLog('UNDERWRITING_ASSESSED', `Assessment complete for loan ${app.loanId}: Decision = ${result.decision}, Rate = ${result.approvedRate}%`);
      });
    }
  }

  fetchPricingQuote() {
    const score = this.selectedApp ? this.selectedApp.creditScore : this.formCreditScore;
    const ltv = this.selectedApp ? this.selectedApp.ltvRatio : this.computedLtv;
    this.loanService.getPricingQuote(score, ltv).subscribe(quote => {
      this.pricingQuoteResult = quote;
    });
  }

  fetchAmortization() {
    const amount = this.selectedApp ? this.selectedApp.loanAmount : this.formLoanAmount;
    const rate = this.pricingQuoteResult ? this.pricingQuoteResult.finalRate : 6.50;
    this.loanService.getAmortizationSchedule(amount, rate, 360).subscribe(schedule => {
      this.amortizationScheduleResult = schedule;
    });
  }

  fetchAccountLookup() {
    this.loanService.getAccountLookupUpdate().subscribe(res => {
      this.accountLookupResult = res;
    });
  }

  fetchTextBlocks() {
    this.loanService.getTextBlocks().subscribe(blocks => {
      this.textBlocks = blocks;
      if (blocks.length > 0 && !this.selectedTextBlock) {
        this.selectedTextBlock = blocks[0];
      }
    });
  }

  selectTextBlock(block: TextBlock) {
    this.selectedTextBlock = block;
  }

  copyTextBlockContent(block: TextBlock) {
    navigator.clipboard.writeText(block.content);
    this.copiedBlockCode = block.code;
    this.addAuditLog('TEXT_BLOCK_COPIED', `Copied multiline text block content for ${block.code}`);
    setTimeout(() => {
      this.copiedBlockCode = null;
    }, 3000);
  }

  submitManualOverride(app: LoanApplication) {
    if (!this.overrideReason) {
      alert('Please enter a justification reason for the underwriting override.');
      return;
    }
    app.status = this.overrideDecision;
    app.decisionReason = `Manual Underwriter Override: ${this.overrideReason}`;
    this.addAuditLog('MANUAL_OVERRIDE', `Applied ${this.overrideDecision} override to loan ${app.loanId}. Justification: ${this.overrideReason}`);
    this.overrideReason = '';
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
    }
  }

  uploadSelectedFile(app: LoanApplication, fileInput: HTMLInputElement) {
    if (!this.selectedFile) return;
    const newDoc = {
      documentId: 'DOC-' + Math.floor(100 + Math.random() * 900),
      fileName: this.selectedFile.name,
      type: this.uploadDocType,
      status: 'VERIFIED'
    };
    app.documents.push(newDoc);
    this.addAuditLog('GRIDFS_UPLOAD', `Uploaded document ${this.selectedFile.name} [${this.uploadDocType}] for loan ${app.loanId}`);
    this.selectedFile = null;
    fileInput.value = '';
  }

  private addAuditLog(action: string, details: string) {
    this.auditLogs.unshift({
      timestamp: new Date(),
      user: this.activeRole,
      action,
      details
    });
  }
}
