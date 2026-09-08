package com.lendingplatform.common.emi;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Plain unit tests for EmiCalculatorService - no Spring context needed
 * (notice there's no @SpringBootTest here), since this class has no
 * dependencies of its own. Fast to run, and exactly the kind of test that
 * should exist for pure calculation logic.
 */
class EmiCalculatorServiceTest {

    private final EmiCalculatorService emiCalculatorService = new EmiCalculatorService();

    @Test
    void calculatesEmiForKnownExample() {
        // Standard textbook example: 100000 principal, 10% p.a., 12 months -> 8791.59
        BigDecimal emi = emiCalculatorService.calculateEmi(BigDecimal.valueOf(100000), BigDecimal.valueOf(10), 12);

        assertThat(emi).isEqualByComparingTo("8791.59");
    }

    @Test
    void higherTenureMeansLowerEmi() {
        // A sanity/property check rather than a fixed value: spreading the
        // same principal over more months must lower the monthly payment.
        BigDecimal emi12 = emiCalculatorService.calculateEmi(BigDecimal.valueOf(200000), BigDecimal.valueOf(12), 12);
        BigDecimal emi24 = emiCalculatorService.calculateEmi(BigDecimal.valueOf(200000), BigDecimal.valueOf(12), 24);

        assertThat(emi24).isLessThan(emi12);
    }

    @Test
    void scheduleFullyAmortizesPrincipalWithZeroFinalBalance() {
        BigDecimal principal = BigDecimal.valueOf(150000);
        List<AmortizationEntry> schedule = emiCalculatorService.generateSchedule(
                principal, BigDecimal.valueOf(11.5), 18, LocalDate.of(2026, 1, 1));

        assertThat(schedule).hasSize(18);

        // The whole point of the reducing-balance schedule: every
        // installment's principal component, summed up, must equal exactly
        // the original loan amount - not a paisa more or less.
        BigDecimal totalPrincipalPaid = schedule.stream()
                .map(AmortizationEntry::principalComponent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalPrincipalPaid).isEqualByComparingTo(principal);

        // And the last installment's remaining balance must be exactly
        // zero - this is what the "last installment absorbs rounding"
        // logic in EmiCalculatorService.generateSchedule() guarantees.
        AmortizationEntry lastInstallment = schedule.get(schedule.size() - 1);
        assertThat(lastInstallment.remainingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(lastInstallment.dueDate()).isEqualTo(LocalDate.of(2027, 7, 1));
    }

    @Test
    void everyInstallmentHasPositiveInterestAndPrincipal() {
        List<AmortizationEntry> schedule = emiCalculatorService.generateSchedule(
                BigDecimal.valueOf(50000), BigDecimal.valueOf(13.2), 6, LocalDate.of(2026, 1, 1));

        assertThat(schedule).allSatisfy(entry -> {
            assertThat(entry.interestComponent()).isGreaterThan(BigDecimal.ZERO);
            assertThat(entry.principalComponent()).isGreaterThan(BigDecimal.ZERO);
        });
    }
}
