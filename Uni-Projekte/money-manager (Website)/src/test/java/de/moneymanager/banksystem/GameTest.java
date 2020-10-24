package de.moneymanager.banksystem;

import de.moneymanager.accounts.AccountServiceImpl;
import de.moneymanager.accounts.BankAccount;
import de.moneymanager.accounts.UserAccount;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class GameTest {

    @Autowired
    private BankSystemService bankSystemService;

    @Autowired
    private AccountServiceImpl accountService;

    @AfterEach
    void endGame() {
        this.bankSystemService.endGame();
    }

    @Test
    void getterAndSetter() {
        //accountFees
        this.bankSystemService.setAccountFees(2000L);
        assertEquals(this.bankSystemService.getAccountFees(), 2000L);

        //BankName
        this.bankSystemService.setBankName("Test");
        assertEquals(this.bankSystemService.getBankName(), "Test");

        //BankStatement
        this.bankSystemService.setBankStatement(true);
        assertTrue(this.bankSystemService.getBankStatement());

        //Bill
        this.bankSystemService.setBill(true);
        assertTrue(this.bankSystemService.getBill());

        //CreditInterest
        this.bankSystemService.setCreditInterest(2.0);
        assertEquals(this.bankSystemService.getCreditInterest(), 2.0);

        //DebitInterest
        this.bankSystemService.setDebitInterest(2.8);
        assertEquals(this.bankSystemService.getDebitInterest(), 2.8);

        //Dispo
        this.bankSystemService.setDispo(1000L);
        assertEquals(this.bankSystemService.getDispo(), 1000L);

        //startBalance
        this.bankSystemService.setStartBalance(10000L);
        assertEquals(this.bankSystemService.getStartBalance(), 10000L);
    }

    @Test
    void startGameEndGame() {
        /*startgame*/
        this.bankSystemService.startGame();
        Assertions.assertTrue(this.bankSystemService.isRunning());

        /*endgame*/
        //Alter settings
        long testLong = 919_287_837L;
        this.bankSystemService.setStartBalance(testLong);

        //create Bank and UserAccount
        this.accountService.createBankAccount("Test");

        this.accountService.createUserAccount("test", "test@test.de");

        //end Game
        this.bankSystemService.endGame();

        //running = false?
        Assertions.assertFalse(this.bankSystemService.isRunning());

        //default settings?
        assertEquals(this.bankSystemService.getStartBalance(), 50_000_00L);

        //accounts deleted?
        Optional<UserAccount> testUser = this.accountService.getUserAccountByEmail("test@test.de");
        Assertions.assertTrue(testUser.isEmpty());

        List<BankAccount> testBankAccounts = this.accountService.getBankAccounts();
        Assertions.assertTrue(testBankAccounts.isEmpty());
    }

    @Test
    void dailyOperations() {
        //settings
        this.bankSystemService.setStartBalance(100L);
        this.bankSystemService.setAccountFees(20L);
        this.bankSystemService.setDebitInterest(0.0);
        this.bankSystemService.setCreditInterest(0.0);

        this.bankSystemService.startGame();

        //create BankAccount
        this.accountService.createBankAccount("Test");

        this.bankSystemService.dailyOperations();

        //test fee
        await().atMost(3, TimeUnit.SECONDS)
               .until(() -> this.accountService.getBankAccounts().get(0).getBalance(), equalTo(80L));

        //test credit
        this.bankSystemService.setAccountFees(0L);
        this.bankSystemService.setCreditInterest(10.0);

        this.bankSystemService.dailyOperations();

        await().atMost(3, TimeUnit.SECONDS)
               .until(() -> this.accountService.getBankAccounts().get(0).getBalance(), equalTo(88L));

        //test debit
        this.bankSystemService.endGame();

        this.bankSystemService.setStartBalance(-100L);
        this.bankSystemService.setAccountFees(0L);
        this.bankSystemService.setDebitInterest(10.0);
        this.bankSystemService.setCreditInterest(0.0);
        this.bankSystemService.setDispo(1000L);

        this.bankSystemService.startGame();

        this.accountService.createBankAccount("Test");

        this.bankSystemService.dailyOperations();

        await().atMost(3, TimeUnit.SECONDS)
               .until(() -> this.accountService.getBankAccounts().get(0).getBalance(), equalTo(-110L));
    }

    @Test
    void testScheduler() {
        LocalDateTime local = LocalDateTime.now().withHour(23).withMinute(59).withSecond(0);
        this.bankSystemService.setTimeInterval(10L, local);

        assertEquals(10L, this.bankSystemService.getTimeInterval());
        assertEquals(local, this.bankSystemService.getFirstInterval());
        assertEquals("23:59", this.bankSystemService.getFormattedFirstInterval());
    }

}
