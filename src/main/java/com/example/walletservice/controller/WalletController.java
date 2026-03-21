package com.example.walletservice.controller;

import com.example.walletservice.dto.WalletRequest;
import com.example.walletservice.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class WalletController {

    private final WalletService service;

    public WalletController(WalletService service) {
        this.service = service;
    }

    @PostMapping("/wallet")
    public void process(@Valid @RequestBody WalletRequest request) {
        service.process(
                request.getWalletId(),
                request.getOperationType(),
                request.getAmount()
        );
    }

    @GetMapping("/wallets/{id}")
    public BigDecimal getBalance(@PathVariable UUID id) {
        return service.getBalance(id);
    }
}
