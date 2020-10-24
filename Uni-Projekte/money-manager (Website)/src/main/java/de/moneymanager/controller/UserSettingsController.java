package de.moneymanager.controller;

import de.moneymanager.accounts.AccountService;
import de.moneymanager.accounts.UserAccount;
import de.moneymanager.controller.tranferobjects.ChangePasswordTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserSettingsController extends AbstractController {

    private final AccountService  accountService;
    private final PasswordEncoder passwordEncoder;

    public UserSettingsController(AccountService accountService, PasswordEncoder passwordEncoder) {
        this.accountService = accountService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/settings")
    public String getSettings(Model model) {
        UserAccount userAccount = this.getUserAccountFromModel(model);
        model.addAttribute("currentUser", userAccount);
        return "settings";
    }

    @GetMapping("/settings/password")
    public String getChangePassword(Model model) {
        return "changePassword";
    }

    @PostMapping("/settings/password")
    public String postChangePassword(Model model, @Validated ChangePasswordTO changePasswordTO) {

        UserAccount userAccount = getUserAccountFromModel(model);

        String currentPassword      = changePasswordTO.getCurrentPassword();
        String newPassword          = changePasswordTO.getNewPassword();
        String newPasswordConfirmed = changePasswordTO.getNewPasswordConfirmed();

        if (!newPassword.equals(newPasswordConfirmed)) {
            model.addAttribute("error", "Passwords dont match!");
            return "changePassword";
        }
        if (newPassword.length() < 6) {
            model.addAttribute("error", "Password has to have at least 6 characters!");
            return "changePassword";
        }

        String password      = userAccount.getPassword();
        String resetPassword = userAccount.getResetPassword();
        if ((resetPassword == null && !this.passwordEncoder.matches(currentPassword, password)) ||
            (resetPassword != null && !resetPassword.equals(currentPassword))) {
            model.addAttribute("error", "Incorrect password");
            return "changePassword";
        }

        this.accountService.setUserAccountPassword(userAccount, newPassword);

        return "redirect:/settings";
    }

}
