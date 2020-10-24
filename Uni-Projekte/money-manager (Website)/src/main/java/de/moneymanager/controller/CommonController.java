package de.moneymanager.controller;

import de.moneymanager.accounts.UserAccount;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CommonController extends AbstractController {

    @GetMapping("/")
    public String showHome(Model model) {
        UserAccount userAccount = getUserAccountFromModel(model);
        return userAccount.getIsAdmin() ? "redirect:/game" : "redirect:/transactions";
    }

    @GetMapping("/login")
    public String showLogin(Model model) {
        return "login";
    }

}
