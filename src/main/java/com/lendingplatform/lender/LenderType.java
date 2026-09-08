package com.lendingplatform.lender;

/**
 * The two kinds of lender this platform embeds. Purely descriptive today -
 * no eligibility rule or pricing logic branches on it - but the schema
 * models it because a real embedded-lending platform would (banks and
 * NBFCs have different regulatory ceilings on interest rates in India).
 */
public enum LenderType {
    BANK,
    NBFC
}
