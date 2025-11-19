package com.paymentchain.transaction.controller;

import com.paymentchain.transaction.entities.Transaction;
import com.paymentchain.transaction.events.TransactionCreatedEvent;
import com.paymentchain.transaction.mapper.TransactionMapper;
import com.paymentchain.transaction.repository.TransactionRepository;
import com.paymentchain.transaction.service.TransactionEventProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionRestController {

   @Autowired
   TransactionRepository transactionRepository;

   @Autowired
   TransactionMapper transactionMapper;

   @Autowired
   TransactionEventProducer eventProducer;

   @GetMapping("/all")
    private List<Transaction> transactionList(){
       return transactionRepository.findAll();
   }

   @GetMapping("/transaction")
    private ResponseEntity<List<Transaction>> get(@RequestParam("accountIban") String accountIban){

       List<Transaction> transactionList = transactionRepository.findByAccountIban(accountIban);

       if(transactionList.isEmpty()){
           return ResponseEntity.noContent().build();
       }
       return ResponseEntity.ok(transactionList);
   }


   /**
    * Creates a new transaction and publishes an event to Kafka.
    *
    * @param transactionInput Transaction data
    * @return Created transaction
    */
   @PostMapping
    public ResponseEntity<?> post(@RequestBody Transaction transactionInput){

       // Save transaction to database
       Transaction saved = transactionRepository.save(transactionInput);

       // Publish event to Kafka
       TransactionCreatedEvent event = TransactionCreatedEvent.of(
               saved.getId(),
               saved.getAccountIban(),
               saved.getAmount()
       );
       eventProducer.publish(event);

       return ResponseEntity.accepted().body(saved);
   }

   @PutMapping("/{id}")
    private ResponseEntity<?> put(@PathVariable("id") long id, @RequestBody Transaction transaction){

       return transactionRepository.findById(id)
               .map(existing -> {
                   transactionMapper.updateTransactionFromRequest(transaction, existing);
                   return ResponseEntity.ok(transactionRepository.save(existing));
               })
              .orElseGet(() -> ResponseEntity.notFound().build());
   }


}
