package de.moneymanager.banksystem;

import de.moneymanager.accounts.AccountService;
import de.moneymanager.accounts.BankAccount;
import de.moneymanager.transaction.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BankSystemServiceTest {

    @Autowired
    BankSystemService bankSystemService;

    @Autowired
    AccountService accountService;

    @Autowired
    GameRepository gameRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    InboxEntryRepository inboxEntryRepository;

    Random rng = new Random();

    @AfterEach
    void tearDown() {
        this.accountService.reset();
        this.gameRepository.deleteAll();
        this.transactionRepository.deleteAll();
        this.inboxEntryRepository.deleteAll();
    }

    @Test
    void inboxTest() {
        this.accountService.reset();
        this.accountService.createBankAccount("name");
        BankAccount bankAccount = this.accountService.getBankAccounts().get(0);

        List<InboxEntry> emptyList = new ArrayList<>();
        assertEquals(this.bankSystemService.getInbox(bankAccount), emptyList);
    }

    @Test
    void startEndGameTest() {
        this.bankSystemService.startGame();

        assertTrue(this.bankSystemService.isRunning());

        this.bankSystemService.endGame();

        assertFalse(this.bankSystemService.isRunning());
    }

    @Test
    void bankNameTest() {
        String name = "name";

        this.bankSystemService.setBankName(name);

        assertEquals(this.bankSystemService.getBankName(), name);
    }

    @Test
    void debitInterestTest() {
        Double debit = this.rng.nextDouble();

        this.bankSystemService.setDebitInterest(debit);

        assertEquals(this.bankSystemService.getDebitInterest(), debit);
    }

    @Test
    void creditInterestTest() {
        Double credit = this.rng.nextDouble();

        this.bankSystemService.setCreditInterest(credit);

        assertEquals(this.bankSystemService.getCreditInterest(), credit);
    }

    @Test
    void bankStatementTest() {
        this.bankSystemService.setBankStatement(true);

        assertTrue(this.bankSystemService.getBankStatement());

        this.bankSystemService.setBankStatement(false);

        assertFalse(this.bankSystemService.getBankStatement());
    }

    @Test
    void billTest() {
        this.bankSystemService.setBill(true);

        assertTrue(this.bankSystemService.getBill());

        this.bankSystemService.setBill(false);

        assertFalse(this.bankSystemService.getBill());
    }

    @Test
    void startBalanceTest() {
        Long startBalance = this.rng.nextLong();

        this.bankSystemService.setStartBalance(startBalance);

        assertEquals(this.bankSystemService.getStartBalance(), startBalance);
    }

    @Test
    void dispoTest() {
        Long dispo = this.rng.nextLong();

        this.bankSystemService.setDispo(dispo);

        assertEquals(this.bankSystemService.getDispo(), dispo);
    }

    @Test
    void accountFeesTest() {
        Long fee = this.rng.nextLong();

        this.bankSystemService.setAccountFees(fee);

        assertEquals(this.bankSystemService.getAccountFees(), fee);
    }

    @Test
    void timeIntervalTest() {
        long intervalLength = 0;
        while (intervalLength <= 0) {
            intervalLength = this.rng.nextLong();
        }
        LocalDateTime intervalStart = LocalDateTime.now().plusMinutes(1);

        this.bankSystemService.setTimeInterval(intervalLength, intervalStart);

        assertEquals(intervalLength, this.bankSystemService.getTimeInterval());

        assertEquals(intervalStart, this.bankSystemService.getFirstInterval());
    }

}
