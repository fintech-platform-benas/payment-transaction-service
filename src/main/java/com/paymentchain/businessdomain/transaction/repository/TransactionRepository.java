package com.paymentchain.businessdomain.transaction.repository;

import com.paymentchain.businessdomain.transaction.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.accountIban = :accountIban")
    List<Transaction> findByAccountIban(@Param("accountIban") String accountIban);


}
