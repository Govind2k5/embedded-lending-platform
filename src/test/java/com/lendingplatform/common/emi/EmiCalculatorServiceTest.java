package com.lendingplatform.common.emi;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        BigDecimal totalPrincipalPaid = schedule.stream()
                .map(AmortizationEntry::principalComponent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalPrincipalPaid).isEqualByComparingTo(principal);

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
