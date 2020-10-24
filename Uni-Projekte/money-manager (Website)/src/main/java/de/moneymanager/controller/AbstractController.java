package de.moneymanager.controller;

import de.moneymanager.accounts.UserAccount;
import org.springframework.ui.Model;

public abstract class AbstractController {

    /**
     * Returns the user that is attached to the model. This user is the user of the current session.
     *
     * @param model the model of the request
     *
     * @return the user attached to the model
     *
     * @see UserControllerAdvice
     */
    protected UserAccount getUserAccountFromModel(Model model) {
        return (UserAccount) model.getAttribute("currentUser");
    }

}
