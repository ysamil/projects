package de.moneymanager.controller;

import de.moneymanager.accounts.AccountService;
import de.moneymanager.accounts.UserAccount;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
public class UserControllerAdvice {

    private final AccountService accountService;

    public UserControllerAdvice(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * This method attaches the current user of the session to the model, so that all request get the inforamtion.
     *
     * @return the UserAccount of the current session.
     */
    @ModelAttribute("currentUser")
    public UserAccount getUserAccount() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<UserAccount> userAccountByEmail = this.accountService.getUserAccountByEmail(email);
        return userAccountByEmail.orElse(null);
    }

}
