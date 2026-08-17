package com.lendingplatform.common.emi;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AmortizationEntry(
        int installmentNumber,
        LocalDate dueDate,
        BigDecimal principalComponent,
        BigDecimal interestComponent,
        BigDecimal emiAmount,
        BigDecimal remainingBalance
) {
}
