package de.moneymanager.transaction;

import de.moneymanager.accounts.BankAccount;
import de.moneymanager.accounts.BankAccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@DataJpaTest
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private BankAccountRepository bankAccountRepository;

    @AfterEach
    void tearDown() {
        this.transactionRepository.deleteAll();
        this.bankAccountRepository.deleteAll();
    }

    @Test
    void shouldInsertAndFind() {
        LocalDateTime now       = LocalDateTime.now();
        LocalDateTime today     = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime yesterday = today.minusDays(1);
        LocalDateTime tomorrow  = today.plusDays(1);

        // Create entities to work with
        BankAccount bankAccount       = this.bankAccountRepository.save(new BankAccount("Test GmbH", "DE123"));
        Transaction sourceTransaction = new Transaction("Test", bankAccount, bankAccount, 2030L, null);
        sourceTransaction.setDate(now);
        this.transactionRepository.save(sourceTransaction);

        // Find the created transaction and check if it is the created one
        Iterable<Transaction> allTransactions = this.transactionRepository.findAllByDateAndBankAccount(
                today, tomorrow, bankAccount);

        List<Transaction> transactions = new LinkedList<>();
        allTransactions.forEach(transactions::add);
        Transaction transaction = transactions.get(0);

        Assertions.assertEquals(1, transactions.size());
        Assertions.assertEquals("Test", transaction.getUsage());

        // Cannot find the transaction because it was created before now.
        allTransactions = this.transactionRepository.findAllByDateAndBankAccount(
                yesterday, today, bankAccount);

        transactions = new LinkedList<>();
        allTransactions.forEach(transactions::add);

        Assertions.assertEquals(0, transactions.size());
    }

}