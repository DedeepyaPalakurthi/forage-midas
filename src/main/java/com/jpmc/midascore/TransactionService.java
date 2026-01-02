package com.jpmc.midascore;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;

@Service
public class TransactionService {

    private static final String INCENTIVE_URL = "http://localhost:8080/incentive";

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate;

    public TransactionService(UserRepository userRepository,
                              TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.restTemplate = new RestTemplate();
    }

    @Transactional
    public void process(Transaction tx) {
        if (tx == null) return;

        UserRecord sender = userRepository.findById(tx.getSenderId()).orElse(null);
        if (sender == null) return;

        UserRecord recipient = userRepository.findById(tx.getRecipientId()).orElse(null);
        if (recipient == null) return;

        float amount = tx.getAmount();
        if (amount <= 0) return;

        if (sender.getBalance() < amount) return;
        System.out.println("TX: " + tx);
        System.out.println("Sender bal before: " + sender.getBalance());
        System.out.println("Recipient bal before: " + recipient.getBalance());


    float incentive = fetchIncentiveAmount(tx);

        sender.setBalance(sender.getBalance() - amount);
        recipient.setBalance(recipient.getBalance() + amount + incentive);

        userRepository.save(sender);
        userRepository.save(recipient);

        TransactionRecord record = new TransactionRecord(sender, recipient, amount);

        // IMPORTANT: Your Task says you must store incentive alongside the transaction.
        // If your TransactionRecord has a different setter/field name, rename this line accordingly.
        record.setIncentive(incentive);

        transactionRepository.save(record);
    }

    private float fetchIncentiveAmount(Transaction tx) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Transaction> request = new HttpEntity<>(tx, headers);

            IncentiveResponse resp = restTemplate.postForObject(
                    INCENTIVE_URL,
                    request,
                    IncentiveResponse.class
            );

            if (resp == null) return 0.0f;
            if (resp.getAmount() < 0) return 0.0f;
            return resp.getAmount();
        } catch (Exception e) {
            return 0.0f;
        }
    }

    public static class IncentiveResponse {
        private float amount;

        public IncentiveResponse() {}

        public float getAmount() {
            return amount;
        }

        public void setAmount(float amount) {
            this.amount = amount;
        }
    }
}
