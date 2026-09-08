-- Flyway migration V1: the entire database schema, created once when the
-- app first starts against an empty database. Flyway tracks that this file
-- has run in its own flyway_schema_history table, so it never runs twice.
-- Hibernate (see application.yml, ddl-auto: validate) only ever checks the
-- JPA entities match what's built here - it never creates or alters
-- anything itself.

-- Borrowers: the people who might take a loan. Referenced by loan_applications,
-- loans and (transitively) repayments via plain FK columns, never joined
-- directly at the application/JPA level.
CREATE TABLE borrowers (
    id                BIGSERIAL PRIMARY KEY,
    name              VARCHAR(150)   NOT NULL,
    email             VARCHAR(150)   NOT NULL UNIQUE,  -- one account per email address
    phone             VARCHAR(20)    NOT NULL,
    date_of_birth     DATE           NOT NULL,
    monthly_income    NUMERIC(15,2)  NOT NULL,          -- checked by IncomeEligibilityRule
    employment_type   VARCHAR(20)    NOT NULL,          -- stores the EmploymentType enum name (SALARIED/SELF_EMPLOYED)
    credit_score      INTEGER        NOT NULL,          -- checked by CreditScoreEligibilityRule
    created_at        TIMESTAMPTZ    NOT NULL
);

-- Lenders: the fictional banks/NBFCs whose eligibility rules and interest
-- rates drive the whole offer-generation flow (see V2 seed data for the 3
-- lenders actually loaded: BankOne, QuickCredit NBFC, PrimeBank).
CREATE TABLE lenders (
    id                      BIGSERIAL PRIMARY KEY,
    name                    VARCHAR(150)   NOT NULL,
    type                    VARCHAR(20)    NOT NULL,     -- LenderType enum name (BANK/NBFC)
    minimum_income          NUMERIC(15,2)  NOT NULL,
    minimum_credit_score    INTEGER        NOT NULL,
    maximum_loan_amount     NUMERIC(15,2)  NOT NULL,
    minimum_tenure_months   INTEGER        NOT NULL,
    maximum_tenure_months   INTEGER        NOT NULL,
    base_interest_rate      NUMERIC(5,2)   NOT NULL,     -- flat annual %, e.g. 11.50 means 11.5% p.a.
    active                  BOOLEAN        NOT NULL DEFAULT TRUE  -- inactive lenders are skipped entirely by eligibility checks
);

-- Merchants: the embedding apps whose customers are the borrowers (e.g.
-- TechKart, TravelNow, EduMart in the seed data). The whole point of
-- "embedded lending" - TechKart never integrates with BankOne directly.
CREATE TABLE merchants (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(150)  NOT NULL,
    active        BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ   NOT NULL
);

-- Loan applications: the record that anchors one borrower's whole journey.
-- status drives LoanApplicationService's whole state machine (see
-- LoanApplicationStatus.java for every value and which ones are actually reachable).
CREATE TABLE loan_applications (
    id                        BIGSERIAL PRIMARY KEY,
    borrower_id               BIGINT         NOT NULL REFERENCES borrowers(id),
    merchant_id               BIGINT         NOT NULL REFERENCES merchants(id),
    requested_amount          NUMERIC(15,2)  NOT NULL,
    requested_tenure_months   INTEGER        NOT NULL,
    status                    VARCHAR(30)    NOT NULL,
    created_at                TIMESTAMPTZ    NOT NULL,
    updated_at                TIMESTAMPTZ    NOT NULL     -- bumped automatically by LoanApplication's @PreUpdate on every status change
);

-- Index the FK (every join starts from a borrower's applications) and the
-- status column (every query in the workflow filters/branches on status).
CREATE INDEX idx_loan_applications_borrower_id ON loan_applications(borrower_id);
CREATE INDEX idx_loan_applications_status ON loan_applications(status);

-- Loan offers: one row per lender that matched eligibility for an
-- application (see EligibilityService + LoanOfferService.generateOffers).
CREATE TABLE loan_offers (
    id                BIGSERIAL PRIMARY KEY,
    application_id    BIGINT         NOT NULL REFERENCES loan_applications(id),
    lender_id         BIGINT         NOT NULL REFERENCES lenders(id),
    approved_amount   NUMERIC(15,2)  NOT NULL,   -- always equal to the application's requested_amount in this project
    interest_rate     NUMERIC(5,2)   NOT NULL,   -- copied from the lender's base_interest_rate at generation time
    tenure_months     INTEGER        NOT NULL,
    monthly_emi       NUMERIC(15,2)  NOT NULL,   -- pre-computed via EmiCalculatorService so the borrower can compare offers directly
    status            VARCHAR(20)    NOT NULL,   -- AVAILABLE / SELECTED / EXPIRED (see OfferStatus.java)
    created_at        TIMESTAMPTZ    NOT NULL
);

CREATE INDEX idx_loan_offers_application_id ON loan_offers(application_id);
CREATE INDEX idx_loan_offers_lender_id ON loan_offers(lender_id);
CREATE INDEX idx_loan_offers_status ON loan_offers(status);

-- Loans: created exactly once per application, from the SELECTED offer, by
-- LoanService.createLoanFromOffer(). The `version` column backs JPA
-- optimistic locking (@Version on Loan.java) - it's what makes two
-- concurrent repayments on the same loan safe without an explicit lock.
CREATE TABLE loans (
    id                  BIGSERIAL PRIMARY KEY,
    application_id      BIGINT         NOT NULL REFERENCES loan_applications(id),
    offer_id            BIGINT         NOT NULL REFERENCES loan_offers(id),
    borrower_id         BIGINT         NOT NULL REFERENCES borrowers(id),
    lender_id           BIGINT         NOT NULL REFERENCES lenders(id),
    principal_amount    NUMERIC(15,2)  NOT NULL,
    interest_rate       NUMERIC(5,2)   NOT NULL,
    tenure_months       INTEGER        NOT NULL,
    monthly_emi         NUMERIC(15,2)  NOT NULL,   -- copied from the offer, not recalculated
    outstanding_amount  NUMERIC(15,2)  NOT NULL,   -- the one mutable balance, decremented by every repayment
    status              VARCHAR(20)    NOT NULL,   -- ACTIVE / COMPLETED (see LoanStatus.java)
    start_date          DATE           NOT NULL,
    maturity_date       DATE           NOT NULL,
    version             BIGINT         NOT NULL DEFAULT 0  -- JPA @Version: optimistic-lock counter, bumped on every update
);

CREATE INDEX idx_loans_borrower_id ON loans(borrower_id);
CREATE INDEX idx_loans_lender_id ON loans(lender_id);
CREATE INDEX idx_loans_status ON loans(status);

-- Repayments: the full EMI schedule for a loan, all inserted at once right
-- after the loan itself is created (see RepaymentService.generateSchedule).
-- The UNIQUE constraint below guarantees a loan can never end up with two
-- rows claiming to be the same installment number.
CREATE TABLE repayments (
    id                   BIGSERIAL PRIMARY KEY,
    loan_id              BIGINT         NOT NULL REFERENCES loans(id),
    installment_number   INTEGER        NOT NULL,
    due_date             DATE           NOT NULL,
    principal_amount     NUMERIC(15,2)  NOT NULL,
    interest_amount      NUMERIC(15,2)  NOT NULL,
    total_amount         NUMERIC(15,2)  NOT NULL,   -- principal + interest for this installment (the EMI, minus rounding drift on the last row)
    paid_amount          NUMERIC(15,2)  NOT NULL DEFAULT 0,
    status               VARCHAR(20)    NOT NULL,   -- PENDING / PARTIAL / PAID (LATE is computed on read, never stored - see RepaymentStatus.java)
    paid_at              TIMESTAMPTZ,
    UNIQUE (loan_id, installment_number)
);

CREATE INDEX idx_repayments_loan_id ON repayments(loan_id);
CREATE INDEX idx_repayments_status ON repayments(status);
