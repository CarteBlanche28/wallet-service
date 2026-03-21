package com.example.walletservice.controller;

import com.example.walletservice.dto.*;
import com.example.walletservice.exception.*;
import com.example.walletservice.service.WalletService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletController.class)
@Import({GlobalExceptionHandler.class, WalletControllerTest.TestConfig.class})
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WalletService walletService;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID walletId = UUID.randomUUID();

    // Успешное пополнение кошелька.
    @Test
    void deposit_success() throws Exception {
        WalletRequest request = new WalletRequest(
                walletId,
                OperationType.DEPOSIT,
                BigDecimal.valueOf(1000)
        );

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(walletService).process(walletId, OperationType.DEPOSIT, BigDecimal.valueOf(1000));
    }

    // Недостаточно средств.
    @Test
    void withdraw_insufficientFunds() throws Exception {
        WalletRequest request = new WalletRequest(
                walletId,
                OperationType.WITHDRAW,
                BigDecimal.valueOf(1000)
        );

        doThrow(new InsufficientFundsException("InsufficientFunds"))
                .when(walletService)
                .process(any(), any(), any());

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("InsufficientFunds"));
    }

    // При попытке поплнения кошелек не найден.
    @Test
    void wallet_not_found() throws Exception {
        WalletRequest request = new WalletRequest(
                walletId,
                OperationType.DEPOSIT,
                BigDecimal.valueOf(1000)
        );

        doThrow(new WalletNotFoundException("Wallet not found"))
                .when(walletService)
                .process(any(), any(), any());

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Wallet not found"));
    }

    // Невалидный JSON.
    @Test
    void invalid_json() throws Exception {
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }

    // Успешная проверка баланса.
    @Test
    void get_balance_success() throws Exception {
        when(walletService.getBalance(walletId))
                .thenReturn(BigDecimal.valueOf(500));

        mockMvc.perform(get("/api/v1/wallets/{id}", walletId))
                .andExpect(status().isOk())
                .andExpect(content().string("500"));
    }

    // При проверке баланса кошелек не найден.
    @Test
    void get_balance_not_found() throws Exception {
        when(walletService.getBalance(walletId))
                .thenThrow(new WalletNotFoundException("Wallet not found"));

        mockMvc.perform(get("/api/v1/wallets/{id}", walletId))
                .andExpect(status().isNotFound());
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public WalletService walletService() {
            return Mockito.mock(WalletService.class);
        }
    }
}
