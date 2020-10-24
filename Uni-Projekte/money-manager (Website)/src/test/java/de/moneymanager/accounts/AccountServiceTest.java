package de.moneymanager.accounts;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AccountServiceTest {

    private static final String USERNAME      = "Test1";
    private static final String EMAIL_ADDRESS = "test@test.de";
    private static final String BANKNAME      = "Bank1";

    @Autowired
    AccountServiceImpl accountService;

    @BeforeEach
    public void createAccounts() {
        this.accountService.createUserAccount(USERNAME, EMAIL_ADDRESS);
        this.accountService.createBankAccount(BANKNAME);

    }

    @AfterEach
    public void clean() {
        this.accountService.reset();
    }

    @Test
    public void testReset() {
        for (int i = 0; i < 10; i++) {
            String username = "test" + i;
            this.accountService.createUserAccount(username, username + "@test.de");
            this.accountService.createBankAccount(username);
        }

        this.accountService.reset();
        assertEquals(this.accountService.getBankAccounts().size(), 0);
        assertEquals(this.accountService.getUserAccounts().size(), 0);
    }

    @Test
    public void testCreateUserAccount() {
        List<UserAccount> userList = this.accountService.getUserAccounts();
        UserAccount       user     = userList.get(0);

        assertEquals(userList.size(), 1, userList.size() > 1 ? "more" : "less" + "than one accounts in database");
        assertEquals(user.getName(), USERNAME, "username is not correct");
        assertEquals(user.getEmail(), EMAIL_ADDRESS, "mail address is not correct");
        assertNotNull(user.getResetPassword(), "no password was set");

        String otherUsername = "Test2";
        this.accountService.createUserAccount(otherUsername, EMAIL_ADDRESS);
        userList = this.accountService.getUserAccounts();
        assertTrue(userList.size() <= 1, "email address is not unique");

    }

    @Test
    public void testEnableAccount() {
        UserAccount userAccount = this.accountService.getUserAccounts().get(0);
        BankAccount bankAccount = this.accountService.getBankAccounts().get(0);

        this.accountService.enableAccount(userAccount, true);
        this.accountService.enableAccount(bankAccount, true);

        assertTrue(userAccount.getEnable());
        assertTrue(bankAccount.getEnable());

        this.accountService.enableAccount(userAccount, false);
        this.accountService.enableAccount(bankAccount, false);

        assertFalse(userAccount.getEnable());
        assertFalse(bankAccount.getEnable());
    }

    @Test
    public void testSetBalance() {
        BankAccount bankAccount = this.accountService.getBankAccounts().get(0);
        long        balance     = 145;

        this.accountService.setBankAccountBalance(bankAccount, balance);
        bankAccount = this.accountService.getBankAccounts().get(0);

        assertEquals(bankAccount.getBalance(), balance);

    }

    @Test
    public void testGetUnlimitedBankAccount() {

        //assertTrue(accountService.getBankAccounts().contains(accountService.getUnlimitedBankAccount()));
    }

    @Test
    public void testCreateBankAccount() {
        assertEquals(this.accountService.getBankAccounts().size(), 1);
        assertEquals(this.accountService.getBankAccounts().get(0).getName(), BANKNAME);
    }

}
