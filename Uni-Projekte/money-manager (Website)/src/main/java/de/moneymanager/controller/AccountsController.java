package de.moneymanager.controller;

import de.moneymanager.accounts.Account;
import de.moneymanager.accounts.AccountService;
import de.moneymanager.accounts.BankAccount;
import de.moneymanager.accounts.UserAccount;
import de.moneymanager.banksystem.BankSystemService;
import de.moneymanager.controller.tranferobjects.BankAccountInfoTO;
import de.moneymanager.controller.tranferobjects.NewBankAccountTO;
import de.moneymanager.controller.tranferobjects.NewUserAccountTO;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@Secured("ROLE_ADMIN")
public class AccountsController extends AbstractController {

    private final AccountService accountService;
    private final BankSystemService bankSystemService;

    public AccountsController(AccountService accountService, BankSystemService bankSystemService) {
        this.accountService = accountService;
        this.bankSystemService = bankSystemService;
    }

    @GetMapping("/users")
    public String showUsers(Model model) {
        if (!this.bankSystemService.isRunning()) {
            return "redirect:/game";
        }
        model.addAttribute("accounts", this.accountService.getUserAccounts());
        return "admin/userAccounts";
    }

    @GetMapping("/users/create")
    public String showCreateUser(Model model) {
        if (!this.bankSystemService.isRunning()) {
            return "redirect:/game";
        }
        model.addAttribute("userAccount", new NewUserAccountTO());
        model.addAttribute("bankAccounts", this.accountService.getBankAccounts());
        return "admin/createUserAccount";
    }

    @PostMapping("/users/create")
    public String saveUserAccount(@Validated @ModelAttribute("userAccount") NewUserAccountTO userAccount, Model model) {
        if (!this.bankSystemService.isRunning()) {
            return "redirect:/game";
        }
        if (userAccount.getName().length() == 0) {
            model.addAttribute("error", "The user name cannot be empty!");
            model.addAttribute("bankAccounts", this.accountService.getBankAccounts().stream().filter(bankAccount -> !this.accountService.getUnlimitedBankAccount().equals(bankAccount)).collect(Collectors.toList()));
            return "admin/createUserAccount";
        }
        if (userAccount.getEmail().length() == 0) {
            model.addAttribute("error", "The email name cannot be empty!");
            model.addAttribute("bankAccounts", this.accountService.getBankAccounts());
            return "admin/createUserAccount";
        }
        if (this.accountService.getUserAccountByEmail(userAccount.getEmail()).isPresent()) {
            model.addAttribute("error", "The email is already in use!");
            model.addAttribute("bankAccounts", this.accountService.getBankAccounts());
            return "admin/createUserAccount";
        }

        Optional<BankAccount> optionalBankAccount = this.accountService.getBankAccountByIban(userAccount.getIban());
        if (optionalBankAccount.isPresent()) {
            BankAccount bankAccount = optionalBankAccount.get();
            this.accountService.createUserAccount(userAccount.getName(), userAccount.getEmail(), bankAccount);
        } else {
            this.accountService.createUserAccount(userAccount.getName(), userAccount.getEmail());
        }

        return "redirect:/users";
    }

    @GetMapping("/accounts")
    public String showBankAccounts(Model model) {
        if (!this.bankSystemService.isRunning()) {
            return "redirect:/game";
        }
        List<BankAccount> accountList = this.accountService.getBankAccounts()
                .stream()
                .filter(bankAccount -> !bankAccount.equals(this.accountService.getUnlimitedBankAccount())
                )
                .collect(Collectors.toList());
        List<BankAccountInfoTO> bankAccounts = accountList.stream().map(bankAccount -> {
            List<String> userAccountNames = bankAccount
                    .getUserAccounts()
                    .stream()
                    .map(Account::getName)
                    .collect(Collectors.toList());

            return new BankAccountInfoTO(
                    bankAccount,
                    userAccountNames
            );
        }).collect(Collectors.toList());

        model.addAttribute("accounts", bankAccounts);
        return "admin/bankAccounts";
    }

    @GetMapping("/accounts/create")
    public String showCreateBankAccount(Model model) {
        if (!this.bankSystemService.isRunning()) {
            return "redirect:/game";
        }
        model.addAttribute("bankAccount", new NewBankAccountTO());
        return "admin/createBankAccount";
    }

    @PostMapping("/accounts/create")
    public String saveBankAccount(@Validated @ModelAttribute("bankAccount") NewBankAccountTO bankAccount, Model model) {
        if (!this.bankSystemService.isRunning()) {
            return "redirect:/game";
        }
        if (bankAccount.getName().length() == 0) {
            model.addAttribute("error", "The bank name cannot be empty!");
            return "admin/createBankAccount";
        }
        this.accountService.createBankAccount(bankAccount.getName());
        return "redirect:/accounts";
    }

    @GetMapping("/accounts/{id}/edit")
    public String showEditBankAccount(@PathVariable Long id, Model model) {
        if (!this.bankSystemService.isRunning()) {
            return "redirect:/game";
        }
        BankAccount bankAccount = this.accountService.getBankAccountById(id).orElse(null);
        if (bankAccount == null) {
            return "redirect:/accounts";
        }

        List<String> userAccountNames = bankAccount
                .getUserAccounts()
                .stream()
                .map(Account::getName)
                .collect(Collectors.toList());

        BankAccountInfoTO bankAccountInfoTO = new BankAccountInfoTO(bankAccount, userAccountNames);

        model.addAttribute("account", bankAccountInfoTO);
        return "admin/editBankAccount";
    }

    @PostMapping("accounts/{id}/edit")
    public String editBankAccount(@PathVariable Long id, @RequestParam String name) {
        if (!this.bankSystemService.isRunning()) {
            return "redirect:/game";
        }
        this.accountService
                .getBankAccountById(id)
                .ifPresent(bankAccount -> this.accountService.setBankAccountName(bankAccount, name));

        return "redirect:/accounts";
    }

    @GetMapping("/user/{userid}")
    public String showUserToAdmin(Model model, @PathVariable("userid") long userid) {
        if (!this.bankSystemService.isRunning()) {
            return "redirect:/game";
        }
        Optional<UserAccount> optionalUserAccount = this.accountService.getUserAccountById(userid);
        if (optionalUserAccount.isPresent()) {
            model.addAttribute("user", optionalUserAccount.get());
            model.addAttribute("bankAccounts", this.accountService.getBankAccounts());
            return "admin/editUserAccount";
        } else {
            return "error";
        }
    }

    @PostMapping("user/makeAdmin")
    public String makeUserAdmin(Model model, @RequestParam long userid) {
        if (!this.bankSystemService.isRunning()) {
            return "redirect:/game";
        }
        Optional<UserAccount> optionalUserAccount = this.accountService.getUserAccountById(userid);
        if (optionalUserAccount.isPresent()) {
            this.accountService.setUserAccountAdmin(optionalUserAccount.get(), true);
            return "redirect:/user/" + userid;
        } else {
            model.addAttribute("message", "Account not found");
            return "error";
        }
    }

    @PostMapping("user/disable")
    public String disableUser(Model model, @RequestParam long userid) {
        if (!this.bankSystemService.isRunning()) {
            return "redirect:/game";
        }
        Optional<UserAccount> optionalUserAccount = this.accountService.getUserAccountById(userid);
        if (optionalUserAccount.isPresent()) {
            this.accountService.enableAccount(optionalUserAccount.get(), false);
            return "redirect:/user/" + userid;
        } else {
            model.addAttribute("message", "Account not found");
            return "error";
        }
    }

    @PostMapping("user/enable")
    public String enableUser(Model model, @RequestParam long userid) {
        if (!this.bankSystemService.isRunning()) {
            return "redirect:/game";
        }
        Optional<UserAccount> optionalUserAccount = this.accountService.getUserAccountById(userid);
        if (optionalUserAccount.isPresent()) {
            this.accountService.enableAccount(optionalUserAccount.get(), true);
            return "redirect:/user/" + userid;
        } else {
            model.addAttribute("message", "Account not found");
            return "error";
        }
    }

}
