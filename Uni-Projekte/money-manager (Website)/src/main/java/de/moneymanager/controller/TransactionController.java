package de.moneymanager.controller;

import de.moneymanager.accounts.AccountService;
import de.moneymanager.accounts.AccountServiceImpl;
import de.moneymanager.accounts.BankAccount;
import de.moneymanager.accounts.UserAccount;
import de.moneymanager.controller.tranferobjects.NewTransactionTO;
import de.moneymanager.controller.tranferobjects.TransactionTO;
import de.moneymanager.transaction.TransactionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class TransactionController extends AbstractController {

    private final TransactionService transactionService;
    private final AccountService     accountService;

    public TransactionController(AccountService accountService, TransactionService transactionService) {
        this.transactionService = transactionService;
        this.accountService = accountService;
    }

    @GetMapping("/transactions")
    public String getBanking(Model model) {
        UserAccount userAccount = this.getUserAccountFromModel(model);
        BankAccount bankAccount = userAccount.getBankAccount();

        List<TransactionTO> transactions =
                this.transactionService
                        .getAccountStatement(bankAccount)
                        .stream()
                        .map(transaction -> {

                            TransactionTO transactionTO = new TransactionTO();
                            transactionTO.setUsage(transaction.getUsage());
                            transactionTO.setCanceled(transaction.getCanceled());

                            LocalDateTime date = transaction.getDate();
                            transactionTO.setDate(
                                    String.format("%02d-%02d-%02d - %02d:%02d:%02d",
                                                  date.getYear(), date.getMonth().getValue(), date.getDayOfMonth(),
                                                  date.getHour(), date.getMinute(), date.getSecond()
                                                 ));

                            BankAccount source      = transaction.getSource();
                            BankAccount destination = transaction.getDestination();

                            if (destination.equals(bankAccount)) {
                                transactionTO.setAmount(transaction.getAmount() / 100.0);
                                transactionTO.setBankAccount(bankAccountToString(source));
                            } else {
                                transactionTO.setAmount(transaction.getAmount() / -100.0);
                                transactionTO.setBankAccount(bankAccountToString(destination));
                            }

                            return transactionTO;
                        })
                        .collect(Collectors.toList());

        model.addAttribute("balance", bankAccount.getBalance());
        model.addAttribute("transactions", transactions);

        return "user/banking";
    }

    private String bankAccountToString(BankAccount bankAccount) {
        return bankAccount.getName() + " - " + bankAccount.getIban();
    }

    @GetMapping("/transactions/new")
    public String showNewTransfer(Model model) {
        UserAccount userAccount = getUserAccountFromModel(model);
        if (userAccount.getBankAccount() != null) {
            model.addAttribute("iban", userAccount.getBankAccount().getIban());
            model.addAttribute("newTransaction", new NewTransactionTO());
            return "user/newTransaction";
        } else {
            model.addAttribute("message", "You do not have a bank account which is assigned to you!");
            return "error";
        }
    }

    @PostMapping("/transactions/new")
    public String sendTransaction(@Validated @ModelAttribute("newTransaction") NewTransactionTO newTransaction,
                                  Model model) {
        UserAccount userAccount = getUserAccountFromModel(model);
        model.addAttribute("iban", userAccount.getBankAccount().getIban());
        if (!AccountServiceImpl.checkIban(newTransaction.getRecipentIban())) {
            model.addAttribute("error", "The Format of the IBAN is false!");
            return "user/newTransaction";
        }
        if (newTransaction.getAmount() == null) {
            model.addAttribute("error", "Please enter an amount for the transaction!");
            return "user/newTransaction";
        }
        if (newTransaction.getUsage().isEmpty()) {
            model.addAttribute("error", "Please enter a usage for the transaction!");
            return "user/newTransaction";
        }
        if (newTransaction.getRecipentIban().equals(userAccount.getBankAccount().getIban())) {
            model.addAttribute("error", "You cannot transfer money to your self!");
            return "user/newTransaction";
        }
        if (this.accountService.getBankAccountByIban(newTransaction.getRecipentIban()).isEmpty()) {
            model.addAttribute("error", "There is no bank account with the entered recipient IBAN");
            return "user/newTransaction";
        }
        model.addAttribute("recipientIban", newTransaction.getRecipentIban());
        model.addAttribute("usage", newTransaction.getUsage());
        model.addAttribute("amount", newTransaction.getAmount());
        long amount = (long) (newTransaction.getAmount() * 100);

        this.transactionService.addTransaction(
                newTransaction.getUsage(),
                userAccount.getBankAccount(),
                newTransaction.getRecipentIban(),
                amount,
                userAccount
                                              );
        return "user/newTransactionResult";
    }

}
