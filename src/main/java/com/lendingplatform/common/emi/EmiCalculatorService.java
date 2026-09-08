package com.lendingplatform.common.emi;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Standard reducing-balance EMI calculation:
 * EMI = P x r x (1+r)^n / ((1+r)^n - 1)
 * where r is the monthly interest rate (annual rate / 12 / 100).
 *
 * Used by two different callers for two different purposes:
 *  - LoanOfferService: calculateEmi() only, to show a borrower what their
 *    monthly payment would be on each lender's offer.
 *  - RepaymentService: generateSchedule(), to build the full installment-by-
 *    installment repayment plan once a loan is actually approved.
 *
 * Every monetary value here is BigDecimal, never double/float - binary
 * floating point cannot exactly represent most decimal fractions (0.1 in
 * double isn't really 0.1), and that error would compound across dozens of
 * installments of arithmetic. Rounding only ever happens at the very end of
 * each calculation, with an explicit RoundingMode, so every rounding
 * decision is deterministic and easy to unit test (see EmiCalculatorServiceTest).
 */
@Service
public class EmiCalculatorService {

    private static final MathContext MC = new MathContext(20); // 20 significant digits of precision for intermediate math
    private static final int MONEY_SCALE = 2; // final results always rounded to 2 decimal places (paise/cents)

    /**
     * Computes the flat monthly EMI for a given principal, annual interest
     * rate and tenure. This single number is then charged every month for
     * the whole tenure (see generateSchedule for how it's split into
     * principal vs. interest each month).
     */
    public BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualInterestRate, int tenureMonths) {
        BigDecimal monthlyRate = monthlyRate(annualInterestRate);

        // Defensive fallback for a 0% interest rate (division by zero would
        // otherwise occur below, since the EMI formula's denominator becomes
        // (1+0)^n - 1 = 0). No seeded lender actually has a 0% rate today,
        // but this keeps the formula safe if one ever did.
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(tenureMonths), MONEY_SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal factor = onePlusR.pow(tenureMonths, MC); // (1+r)^n

        BigDecimal numerator = principal.multiply(monthlyRate).multiply(factor);
        BigDecimal denominator = factor.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, MONEY_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Builds a full amortization schedule using the reducing balance method.
     * The last installment absorbs any leftover paise so the balance reaches exactly zero.
     */
    public List<AmortizationEntry> generateSchedule(BigDecimal principal, BigDecimal annualInterestRate,
                                                      int tenureMonths, LocalDate startDate) {
        BigDecimal monthlyRate = monthlyRate(annualInterestRate);
        BigDecimal emi = calculateEmi(principal, annualInterestRate, tenureMonths); // same flat EMI every month

        List<AmortizationEntry> schedule = new ArrayList<>();
        BigDecimal balance = principal; // shrinks by principalComponent every iteration

        for (int month = 1; month <= tenureMonths; month++) {
            // Interest is charged on whatever balance is still outstanding
            // THIS month - this is what makes it "reducing balance" rather
            // than a flat/even split: as the balance shrinks, so does the
            // interest portion of each future EMI.
            BigDecimal interestComponent = balance.multiply(monthlyRate).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            BigDecimal principalComponent = emi.subtract(interestComponent);
            BigDecimal emiForInstallment = emi;

            // Rounding every month accumulates a small drift over a 12/24/60-
            // month schedule (a few paise here and there). Rather than track
            // a separate rounding-adjustment account, we force the LAST
            // installment's principal to equal whatever balance is actually
            // left, guaranteeing the schedule always lands on exactly 0.00
            // instead of finishing a few paise over or short.
            boolean isLastInstallment = month == tenureMonths;
            if (isLastInstallment) {
                principalComponent = balance;
                emiForInstallment = principalComponent.add(interestComponent);
            }

            balance = balance.subtract(principalComponent);

            schedule.add(new AmortizationEntry(
                    month,
                    startDate.plusMonths(month),
                    principalComponent,
                    interestComponent,
                    emiForInstallment,
                    balance
            ));
        }

        return schedule;
    }

    /** Converts an annual percentage rate (e.g. 11.5 for 11.5% p.a.) into a monthly fraction, e.g. 11.5/1200. */
    private BigDecimal monthlyRate(BigDecimal annualInterestRate) {
        return annualInterestRate.divide(BigDecimal.valueOf(1200), MC);
    }
}
