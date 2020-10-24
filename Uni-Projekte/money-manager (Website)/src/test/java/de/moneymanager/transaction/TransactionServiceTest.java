package de.moneymanager.transaction;

import de.moneymanager.accounts.AccountService;
import de.moneymanager.accounts.BankAccount;
import de.moneymanager.accounts.UserAccount;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private AccountService     accountService;

    @AfterEach
    void tearDown() {
        this.transactionService.reset();
        this.accountService.reset();
    }

    @Test
    void addTransaction() {
        this.accountService.createBankAccount("Test GmbH");
        this.accountService.createBankAccount("Other GmbH");
        List<BankAccount> bankAccounts = this.accountService.getBankAccounts();
        BankAccount       bankAccount1 = bankAccounts.get(0);
        BankAccount       bankAccount2 = bankAccounts.get(1);

        assertNotNull(bankAccount1);
        assertNotNull(bankAccount2);

        this.accountService.createUserAccount("Max", "max@max.de", bankAccount1);
        UserAccount userAccount = this.accountService.getUserAccountByEmail("max@max.de").orElseThrow();
        assertNotNull(userAccount);

        assertThrows(IllegalArgumentException.class, () -> {
            this.transactionService.addTransaction("Test", bankAccount1, bankAccount2.getIban(), -2010, null);
        });

        List<Transaction> accountStatement = this.transactionService.getAccountStatement(bankAccount1);
        assertEquals(0, accountStatement.size());

        long amount = 2030L;
        this.transactionService.addTransaction("Test", bankAccount1, bankAccount2, amount, userAccount);

        await().atMost(3, TimeUnit.SECONDS)
               .until(() -> this.transactionService.getAccountStatement(bankAccount1).size(), equalTo(1));

        await().atMost(3, TimeUnit.SECONDS)
               .until(() -> this.transactionService.getAccountStatement(bankAccount1).get(0).getAmount(),
                      equalTo(amount)
                     );

    }

}