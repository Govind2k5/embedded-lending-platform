-- Fictional sample data for local development / demos only.

INSERT INTO borrowers (name, email, phone, date_of_birth, monthly_income, employment_type, credit_score, created_at) VALUES
('Rohan Mehta', 'rohan.mehta@example.com', '9800000001', '1996-03-14', 55000.00, 'SALARIED', 720, NOW()),
('Priya Sharma', 'priya.sharma@example.com', '9800000002', '1998-07-22', 30000.00, 'SALARIED', 660, NOW()),
('Amit Verma', 'amit.verma@example.com', '9800000003', '1990-11-02', 80000.00, 'SELF_EMPLOYED', 780, NOW()),
('Sneha Iyer', 'sneha.iyer@example.com', '9800000004', '2000-01-30', 22000.00, 'SALARIED', 610, NOW()),
('Karthik Nair', 'karthik.nair@example.com', '9800000005', '1994-09-18', 45000.00, 'SELF_EMPLOYED', 700, NOW());

INSERT INTO lenders (name, type, minimum_income, minimum_credit_score, maximum_loan_amount, minimum_tenure_months, maximum_tenure_months, base_interest_rate, active) VALUES
('BankOne', 'BANK', 40000.00, 700, 500000.00, 6, 60, 11.50, TRUE),
('QuickCredit NBFC', 'NBFC', 25000.00, 650, 200000.00, 3, 36, 13.20, TRUE),
('PrimeBank', 'BANK', 60000.00, 750, 1000000.00, 12, 84, 10.80, TRUE);

INSERT INTO merchants (name, active, created_at) VALUES
('TechKart', TRUE, NOW()),
('TravelNow', TRUE, NOW()),
('EduMart', TRUE, NOW());
