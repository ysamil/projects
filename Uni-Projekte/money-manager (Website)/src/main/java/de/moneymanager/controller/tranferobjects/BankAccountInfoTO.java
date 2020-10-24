package de.moneymanager.controller.tranferobjects;

import de.moneymanager.accounts.BankAccount;
import lombok.Data;

import java.util.List;

@Data
public class BankAccountInfoTO {

    private BankAccount  bankAccount;
    private List<String> userAccounts;

    public BankAccountInfoTO(BankAccount bankAccount, List<String> userAccounts) {
        this.bankAccount = bankAccount;
        this.userAccounts = userAccounts;
    }

}
