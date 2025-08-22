package com.examly.springapp.repository;

import com.examly.springapp.entity.Transaction;
import com.examly.springapp.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySenderWalletOrReceiverWalletOrderByTransactionDateDesc(Wallet senderWallet, Wallet receiverWallet);
}
