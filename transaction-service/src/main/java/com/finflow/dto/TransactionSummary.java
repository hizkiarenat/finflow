package com.finflow.dto;

public interface TransactionSummary {

    String getType();

    Long getTotalCount();

    java.math.BigDecimal getTotalAmount();
}