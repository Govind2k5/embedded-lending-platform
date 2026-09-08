-- Flyway migration V2: fictional sample data for local development / demos
-- only. Runs once, right after V1 builds the schema, every time the app
-- starts against a fresh database (Docker Compose's Postgres volume, or a
-- freshly-created Testcontainers database in the integration tests).
--
-- These are NOT real people, banks, or companies. All names, incomes,
-- credit scores and lending terms below are made up for demonstration
-- purposes - see the project README for the "no real integrations" disclaimer.

-- 5 borrowers spanning a range of income/credit-score combinations, so the
-- seeded data can demonstrate every eligibility outcome without creating
-- test fixtures by hand (see LoanWorkflowIntegrationTest, which uses
-- borrower id 1 directly):
--   Rohan Mehta   - solidly eligible for BankOne and QuickCredit NBFC
--   Priya Sharma  - only eligible for QuickCredit NBFC (lower income/score)
--   Amit Verma    - eligible for all three lenders (highest income/score)
--   Sneha Iyer    - below every lender's minimum (income 22k, score 610) - always REJECTED
--   Karthik Nair  - borderline: eligible for QuickCredit NBFC only
INSERT INTO borrowers (name, email, phone, date_of_birth, monthly_income, employment_type, credit_score, created_at) VALUES
('Rohan Mehta', 'rohan.mehta@example.com', '9800000001', '1996-03-14', 55000.00, 'SALARIED', 720, NOW()),
('Priya Sharma', 'priya.sharma@example.com', '9800000002', '1998-07-22', 30000.00, 'SALARIED', 660, NOW()),
('Amit Verma', 'amit.verma@example.com', '9800000003', '1990-11-02', 80000.00, 'SELF_EMPLOYED', 780, NOW()),
('Sneha Iyer', 'sneha.iyer@example.com', '9800000004', '2000-01-30', 22000.00, 'SALARIED', 610, NOW()),
('Karthik Nair', 'karthik.nair@example.com', '9800000005', '1994-09-18', 45000.00, 'SELF_EMPLOYED', 700, NOW());

-- 3 fictional lenders with deliberately different eligibility bars and rates,
-- so a single application can plausibly match 0, 1, 2 or all 3 depending on
-- the borrower - this is what makes the eligibility engine's output
-- interesting to demo rather than always all-or-nothing.
INSERT INTO lenders (name, type, minimum_income, minimum_credit_score, maximum_loan_amount, minimum_tenure_months, maximum_tenure_months, base_interest_rate, active) VALUES
('BankOne', 'BANK', 40000.00, 700, 500000.00, 6, 60, 11.50, TRUE),
('QuickCredit NBFC', 'NBFC', 25000.00, 650, 200000.00, 3, 36, 13.20, TRUE),
('PrimeBank', 'BANK', 60000.00, 750, 1000000.00, 12, 84, 10.80, TRUE);

-- 3 fictional merchant apps embedding this lending platform.
INSERT INTO merchants (name, active, created_at) VALUES
('TechKart', TRUE, NOW()),
('TravelNow', TRUE, NOW()),
('EduMart', TRUE, NOW());
