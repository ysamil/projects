package de.moneymanager;

import de.moneymanager.accounts.AccountService;
import de.moneymanager.accounts.UserAccount;
import de.moneymanager.auth.AuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class StartupRunner implements CommandLineRunner {

    Logger logger = LoggerFactory.getLogger(StartupRunner.class);

    private final AccountService accountService;

    public StartupRunner(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void run(String... args) {
        String email    = "admin@admin.com";
        String password = AuthUtils.generatePassword();

        Optional<UserAccount> optionalUserAccount = this.accountService.getUserAccountByEmail(email);

        if (optionalUserAccount.isPresent()) {
            return;
        }

        this.accountService.createUserAccount("System-Admin", email);
        optionalUserAccount = this.accountService.getUserAccountByEmail(email);
        if (optionalUserAccount.isPresent()) {
            UserAccount userAccount = optionalUserAccount.get();
            this.accountService.setUserAccountAdmin(userAccount, true);
            this.accountService.setUserAccountPassword(userAccount, password);

            this.logger.info("A default admin user was created with EMAIL=" + email + " and PASSWORD=" + password);
        }

    }

}
