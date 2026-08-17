-- Borrowers
CREATE TABLE borrowers (
    id                BIGSERIAL PRIMARY KEY,
    name              VARCHAR(150)   NOT NULL,
    email             VARCHAR(150)   NOT NULL UNIQUE,
    phone             VARCHAR(20)    NOT NULL,
    date_of_birth     DATE           NOT NULL,
    monthly_income    NUMERIC(15,2)  NOT NULL,
    employment_type   VARCHAR(20)    NOT NULL,
    credit_score      INTEGER        NOT NULL,
    created_at        TIMESTAMPTZ    NOT NULL
);

-- Lenders
CREATE TABLE lenders (
    id                      BIGSERIAL PRIMARY KEY,
    name                    VARCHAR(150)   NOT NULL,
    type                    VARCHAR(20)    NOT NULL,
    minimum_income          NUMERIC(15,2)  NOT NULL,
    minimum_credit_score    INTEGER        NOT NULL,
    maximum_loan_amount     NUMERIC(15,2)  NOT NULL,
    minimum_tenure_months   INTEGER        NOT NULL,
    maximum_tenure_months   INTEGER        NOT NULL,
    base_interest_rate      NUMERIC(5,2)   NOT NULL,
    active                  BOOLEAN        NOT NULL DEFAULT TRUE
);

-- Merchants
CREATE TABLE merchants (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(150)  NOT NULL,
    active        BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ   NOT NULL
);

-- Loan applications
CREATE TABLE loan_applications (
    id                        BIGSERIAL PRIMARY KEY,
    borrower_id               BIGINT         NOT NULL REFERENCES borrowers(id),
    merchant_id               BIGINT         NOT NULL REFERENCES merchants(id),
    requested_amount          NUMERIC(15,2)  NOT NULL,
    requested_tenure_months   INTEGER        NOT NULL,
    status                    VARCHAR(30)    NOT NULL,
    created_at                TIMESTAMPTZ    NOT NULL,
    updated_at                TIMESTAMPTZ    NOT NULL
);

CREATE INDEX idx_loan_applications_borrower_id ON loan_applications(borrower_id);
CREATE INDEX idx_loan_applications_status ON loan_applications(status);

-- Loan offers
CREATE TABLE loan_offers (
    id                BIGSERIAL PRIMARY KEY,
    application_id    BIGINT         NOT NULL REFERENCES loan_applications(id),
    lender_id         BIGINT         NOT NULL REFERENCES lenders(id),
    approved_amount   NUMERIC(15,2)  NOT NULL,
    interest_rate     NUMERIC(5,2)   NOT NULL,
    tenure_months     INTEGER        NOT NULL,
    monthly_emi       NUMERIC(15,2)  NOT NULL,
    status            VARCHAR(20)    NOT NULL,
    created_at        TIMESTAMPTZ    NOT NULL
);

CREATE INDEX idx_loan_offers_application_id ON loan_offers(application_id);
CREATE INDEX idx_loan_offers_lender_id ON loan_offers(lender_id);
CREATE INDEX idx_loan_offers_status ON loan_offers(status);

-- Loans
CREATE TABLE loans (
    id                  BIGSERIAL PRIMARY KEY,
    application_id      BIGINT         NOT NULL REFERENCES loan_applications(id),
    offer_id            BIGINT         NOT NULL REFERENCES loan_offers(id),
    borrower_id         BIGINT         NOT NULL REFERENCES borrowers(id),
    lender_id           BIGINT         NOT NULL REFERENCES lenders(id),
    principal_amount    NUMERIC(15,2)  NOT NULL,
    interest_rate       NUMERIC(5,2)   NOT NULL,
    tenure_months       INTEGER        NOT NULL,
    monthly_emi         NUMERIC(15,2)  NOT NULL,
    outstanding_amount  NUMERIC(15,2)  NOT NULL,
    status              VARCHAR(20)    NOT NULL,
    start_date          DATE           NOT NULL,
    maturity_date       DATE           NOT NULL,
    version             BIGINT         NOT NULL DEFAULT 0
);

CREATE INDEX idx_loans_borrower_id ON loans(borrower_id);
CREATE INDEX idx_loans_lender_id ON loans(lender_id);
CREATE INDEX idx_loans_status ON loans(status);

-- Repayments
CREATE TABLE repayments (
    id                   BIGSERIAL PRIMARY KEY,
    loan_id              BIGINT         NOT NULL REFERENCES loans(id),
    installment_number   INTEGER        NOT NULL,
    due_date             DATE           NOT NULL,
    principal_amount     NUMERIC(15,2)  NOT NULL,
    interest_amount      NUMERIC(15,2)  NOT NULL,
    total_amount         NUMERIC(15,2)  NOT NULL,
    paid_amount          NUMERIC(15,2)  NOT NULL DEFAULT 0,
    status               VARCHAR(20)    NOT NULL,
    paid_at              TIMESTAMPTZ,
    UNIQUE (loan_id, installment_number)
);

CREATE INDEX idx_repayments_loan_id ON repayments(loan_id);
CREATE INDEX idx_repayments_status ON repayments(status);
