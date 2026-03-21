package com.example.walletservice.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public class WalletRequest {

    @NotNull
    private UUID walletId;

    @NotNull
    private OperationType operationType;

    @NotNull
    @Positive
    private BigDecimal amount;

    public WalletRequest() {}

    public WalletRequest(UUID walletId, OperationType operationType, BigDecimal amount) {
        this.walletId = walletId;
        this.operationType = operationType;
        this.amount = amount;
    }

    public UUID getWalletId() { return walletId; }
    public OperationType getOperationType() { return operationType; }
    public BigDecimal getAmount() { return amount; }
}
