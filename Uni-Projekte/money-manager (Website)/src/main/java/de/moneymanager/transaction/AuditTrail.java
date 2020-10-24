package de.moneymanager.transaction;

import de.moneymanager.accounts.BankAccount;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
class AuditTrail {

    private final TransactionRepository repository;

    public AuditTrail(TransactionRepository repository) {
        this.repository = repository;
    }

    /**
     * Requests the corresponding transactions for an bank account.
     *
     * @param bankAccount bank account from which account statements are requested
     *
     * @return list of transaction belonging to bankAccount
     */
    public List<Transaction> getAccountStatement(BankAccount bankAccount) {
        List<Transaction> result = new ArrayList<>();
        Iterable<Transaction> statements =
                this.repository.findAllByDestinationOrSourceOrderByDateDesc(bankAccount, bankAccount);
        statements.forEach(result::add);
        return result;
    }

    /**
     * Calculates the current balance of the bank account. All transactions from the database belonging to the
     * corresponding bank account are evaluated
     *
     * @param bankAccount bank account from which the balance should be calculated
     *
     * @return current balance of bankAccount
     */
    public double calculateBalance(BankAccount bankAccount) {
        double            balance          = 0D;
        List<Transaction> accountStatement = getAccountStatement(bankAccount);
        for (Transaction t : accountStatement) {
            if (t.getSource() == bankAccount) {
                balance -= t.getAmount();
            } else {
                balance += t.getAmount();
            }
        }

        return balance;
    }

    public List<Transaction> getAccountStatement(BankAccount bankAccount, LocalDateTime begin, LocalDateTime end) {
        List<Transaction> result = new ArrayList<>();
        Iterable<Transaction> statements =
                this.repository.findAllByDateAndBankAccount(begin, end, bankAccount);
        statements.forEach(result::add);
        return result;
    }

}
