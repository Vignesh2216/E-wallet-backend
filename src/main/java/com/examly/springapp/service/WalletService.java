package com.examly.springapp.service;

import com.examly.springapp.entity.*;
import com.examly.springapp.repository.TransactionRepository;
import com.examly.springapp.repository.UserRepository;
import com.examly.springapp.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Wallet createWallet(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if wallet already exists
        if (walletRepository.findByUser(user).isPresent()) {
            // Return existing wallet instead of throwing exception
            return walletRepository.findByUser(user).get();
        }
        
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setIsActive(true);
        
        return walletRepository.save(wallet);
    }

    public Wallet getWalletByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
    }

    public Wallet getWalletByWalletNumber(String walletNumber) {
        return walletRepository.findByWalletNumber(walletNumber)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
    }

    @Transactional
    public Transaction topUpWallet(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }

        Wallet wallet = getWalletByUserId(userId);
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        Transaction transaction = new Transaction();
        transaction.setSenderWallet(wallet);
        transaction.setReceiverWallet(wallet);
        transaction.setAmount(amount);
        transaction.setTransactionType("TOPUP");
        transaction.setDescription("Wallet top-up");

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction transferMoney(Long senderUserId, String receiverEmail, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }

        Wallet senderWallet = getWalletByUserId(senderUserId);
        User receiverUser = userRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> new RuntimeException("user not found"));
        Wallet receiverWallet = getWalletByUserId(receiverUser.getId());

        if (senderWallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // Deduct from sender
        senderWallet.setBalance(senderWallet.getBalance().subtract(amount));
        walletRepository.save(senderWallet);

        // Add to receiver
        receiverWallet.setBalance(receiverWallet.getBalance().add(amount));
        walletRepository.save(receiverWallet);

        Transaction transaction = new Transaction();
        transaction.setSenderWallet(senderWallet);
        transaction.setReceiverWallet(receiverWallet);
        transaction.setAmount(amount);
        transaction.setTransactionType("TRANSFER");
        transaction.setDescription("Money transfer to " + receiverEmail);

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction withdrawMoney(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }

        Wallet wallet = getWalletByUserId(userId);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        Transaction transaction = new Transaction();
        transaction.setSenderWallet(wallet);
        transaction.setReceiverWallet(wallet);
        transaction.setAmount(amount);
        transaction.setTransactionType("WITHDRAWAL");
        transaction.setDescription("Money withdrawn");

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactionHistory(Long userId) {
        Wallet wallet = getWalletByUserId(userId);
        return transactionRepository.findBySenderWalletOrReceiverWalletOrderByTransactionDateDesc(wallet, wallet);
    }

    public BigDecimal getBalance(Long userId) {
        Wallet wallet = getWalletByUserId(userId);
        return wallet.getBalance();
    }
}
