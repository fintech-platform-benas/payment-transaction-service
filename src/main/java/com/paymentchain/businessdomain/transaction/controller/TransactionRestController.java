package com.paymentchain.businessdomain.transaction.controller;

import com.paymentchain.businessdomain.transaction.entities.Transaction;
import com.paymentchain.businessdomain.transaction.mapper.TransactionMapper;
import com.paymentchain.businessdomain.transaction.repository.TransactionRepository;
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


   @PostMapping
    public ResponseEntity<?> post(@RequestBody Transaction transactionInput){

       transactionRepository.save(transactionInput);
       return ResponseEntity.accepted().body(transactionInput);
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
