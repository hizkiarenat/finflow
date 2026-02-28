package com.finflow.dto;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountSummaryProjection {

    UUID getUserId();

    Long getTotalAccounts();

    BigDecimal getTotalBalance();

    String getCurrency();
}
