package de.moneymanager.accounts;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AccountServiceImplTest {

    @Autowired
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        this.accountService.reset();
    }

    @AfterEach
    public void resetAll() {
        this.accountService.reset();
    }

    /**
     * Checks if the {@link de.moneymanager.accounts.UserAccountRepository} and {@link
     * de.moneymanager.accounts.BankAccountRepository} are empty.
     *
     * @return true if both are empty and false if one is not empty.
     */
    private boolean repositoriesEmpty() {
        return this.accountService.getBankAccounts().isEmpty() && this.accountService.getUserAccounts().isEmpty();
    }

    @Test
    public void createUserAccountTest() {
        String name  = "testName";
        String email = "test@email.com";

        //Before an account is created the repositories should be empty.
        assertTrue(repositoriesEmpty());

        //Create a new userAccount.
        this.accountService.createUserAccount(name, email);

        //After the creation the userAccountRepository should not be empty.
        assertFalse(this.accountService.getUserAccounts().isEmpty());
    }

    @Test
    public void createUserAccountWithBankAccountTest() {
        String name  = "testName";
        String email = "test@email.com";
        this.accountService.createBankAccount("testBankAccount");
        BankAccount bankAccount = this.accountService.getBankAccounts().get(0);

        //Before a userAccount is created the userAccountRepositories should be empty.
        assertTrue(this.accountService.getUserAccounts().isEmpty());

        //Create a new userAccount.
        this.accountService.createUserAccount(name, email, bankAccount);

        //After the creation the userAccountRepository should not be empty.
        assertFalse(this.accountService.getUserAccounts().isEmpty());
    }

    @Test
    public void createBankAccountTest() {
        String name = "test";

        //Before an account is created the repositories should be empty.
        assertTrue(repositoriesEmpty());

        //Create a new bankAccount
        this.accountService.createBankAccount(name);

        //After the creation the bankAccountRepository should not be empty.
        assertFalse(this.accountService.getBankAccounts().isEmpty());
    }

    @Test
    public void repositoriesEmptyAfterReset() {
        String name  = "test";
        String email = "test@email.com";

        //Before an Account is created the repositories should be empty.
        assertTrue(repositoriesEmpty());

        //Create a new userAccount and a new bankAccount.
        this.accountService.createUserAccount(name, email);
        this.accountService.createBankAccount(name);

        //After the creation both repositories should not be empty.
        assertFalse(this.accountService.getUserAccounts().isEmpty());
        assertFalse(this.accountService.getBankAccounts().isEmpty());

        //Reset the repositories.
        this.accountService.reset();

        //After the reset both repositories should be empty again.
        assertTrue(repositoriesEmpty());
    }

    @Test
    public void getUnlimitedBankAccountTest() {
        //Get an unlimitedBankAccount.
        BankAccount account = this.accountService.getUnlimitedBankAccount();

        //The IBAN of the unlimitedBankAccount should be DE00000000000000000000.
        assertEquals(account.getIban(), "DE00000000000000000000");

        //The name of the unlimitedBankAccount should be "Unlimited".
        assertEquals(account.getName(), "Bank");
    }

}
