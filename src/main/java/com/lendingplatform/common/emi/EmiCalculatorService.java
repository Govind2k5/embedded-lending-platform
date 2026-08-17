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
 */
@Service
public class EmiCalculatorService {

    private static final MathContext MC = new MathContext(20);
    private static final int MONEY_SCALE = 2;

    public BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualInterestRate, int tenureMonths) {
        BigDecimal monthlyRate = monthlyRate(annualInterestRate);

        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(tenureMonths), MONEY_SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal factor = onePlusR.pow(tenureMonths, MC);

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
        BigDecimal emi = calculateEmi(principal, annualInterestRate, tenureMonths);

        List<AmortizationEntry> schedule = new ArrayList<>();
        BigDecimal balance = principal;

        for (int month = 1; month <= tenureMonths; month++) {
            BigDecimal interestComponent = balance.multiply(monthlyRate).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            BigDecimal principalComponent = emi.subtract(interestComponent);
            BigDecimal emiForInstallment = emi;

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

    private BigDecimal monthlyRate(BigDecimal annualInterestRate) {
        return annualInterestRate.divide(BigDecimal.valueOf(1200), MC);
    }
}
