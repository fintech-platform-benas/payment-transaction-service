package com.paymentchain.businessdomain.transaction.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class Transaction {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private long id;
    private String reference;
    //@Column(name = "accountIban")
    private String accountIban;
    private LocalDateTime date;
    private double amount;
    private double fee;
    private String description;
    private Status status;
    private Channel channel;

}
