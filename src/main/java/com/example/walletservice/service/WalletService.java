package com.example.walletservice.service;

import com.example.walletservice.dto.OperationType;
import com.example.walletservice.entity.Wallet;
import com.example.walletservice.exception.InsufficientFundsException;
import com.example.walletservice.exception.WalletNotFoundException;
import com.example.walletservice.repository.WalletRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository repository;

    public WalletService(WalletRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void process(UUID walletId, OperationType type, BigDecimal amount) {

        Wallet wallet = repository.findByIdForUpdate(walletId)
                .orElse(new Wallet(walletId, BigDecimal.ZERO));

        if (type == OperationType.DEPOSIT) {
            wallet.setBalance(wallet.getBalance().add(amount));
        } else {
            if (wallet.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException("Insufficient funds");
            }
            wallet.setBalance(wallet.getBalance().subtract(amount));
        }

        repository.save(wallet);
    }

    public BigDecimal getBalance(UUID walletId) {
        return repository.findById(walletId)
                .map(Wallet::getBalance)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
    }
}
