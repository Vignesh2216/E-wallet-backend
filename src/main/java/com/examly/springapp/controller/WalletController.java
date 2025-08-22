package com.examly.springapp.controller;

import com.examly.springapp.entity.Transaction;
import com.examly.springapp.entity.Wallet;
import com.examly.springapp.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WalletController {

    @Autowired
    private WalletService walletService;

    @PostMapping("/create")
    public ResponseEntity<?> createWallet(@RequestParam Long userId) {
        try {
            Wallet wallet = walletService.createWallet(userId);
            return ResponseEntity.ok(wallet);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(@RequestParam Long userId) {
        try {
            BigDecimal balance = walletService.getBalance(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("balance", balance);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/topup")
    public ResponseEntity<?> topUpWallet(@RequestParam Long userId, @RequestBody Map<String, Object> request) {
        try {
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            Transaction transaction = walletService.topUpWallet(userId, amount);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transferMoney(@RequestParam Long senderUserId, @RequestBody Map<String, Object> request) {
        try {
            String receiverEmail = request.get("receiverEmail").toString();
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            Transaction transaction = walletService.transferMoney(senderUserId, receiverEmail, amount);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{userId}/withdraw")
    public ResponseEntity<Transaction> withdrawMoney(@PathVariable Long userId, @RequestBody Map<String, Object> request) {
        try {
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            Transaction transaction = walletService.withdrawMoney(userId, amount);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null); // Return null for the body to match the expected type
        }
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactionHistory(@RequestParam Long userId) {
        try {
            List<Transaction> transactions = walletService.getTransactionHistory(userId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/wallet-info")
    public ResponseEntity<?> getWalletInfo(@RequestParam Long userId) {
        try {
            Wallet wallet = walletService.getWalletByUserId(userId);
            return ResponseEntity.ok(wallet);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
