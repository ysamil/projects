package de.moneymanager.transaction;

import de.moneymanager.accounts.BankAccount;
import de.moneymanager.accounts.UserAccount;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    private final AuditTrail       auditTrail;
    private final TransactionMangr transactionMangr;

    public TransactionService(AuditTrail auditTrail, TransactionMangr transactionMangr) {
        this.auditTrail = auditTrail;
        this.transactionMangr = transactionMangr;
    }

    /**
     * Converts the specified data into a transaction and queues it to be executed. If parameters are invalid or the
     * iban is not in the right format  it throws an IllegalArgumentException.
     *
     * @param usage           usage of transaction
     * @param source          bank account from which the amount is to be debited
     * @param destinationIban bank accounts iban to which the amount should be credited
     * @param amount          amount to be transferred
     * @param user            user account that wants to perform the transaction
     */
    public void addTransaction(String usage, BankAccount source, String destinationIban, long amount,
                               UserAccount user) throws IllegalArgumentException {
        this.transactionMangr.addTransaction(usage, source, destinationIban, amount, user);
    }

    /**
     * Converts the specified data into a transaction and queues it to be executed.
     *
     * @param usage       usage of transaction
     * @param source      bank account from which the amount is to be debited
     * @param destination bank account to which the amount should be credited
     * @param amount      amount to be transferred
     * @param user        user account that wants to perform the transaction
     */
    public void addTransaction(String usage, BankAccount source, BankAccount destination, long amount,
                               UserAccount user) throws IllegalArgumentException {
        this.transactionMangr.addTransaction(usage, source, destination, amount, user);
    }

    /**
     * Requests the corresponding transactions for an bank account.
     *
     * @param bankAccount bank account from which account statements are requested
     *
     * @return list of transaction belonging to bankAccount
     */
    public List<Transaction> getAccountStatement(BankAccount bankAccount) {
        return this.auditTrail.getAccountStatement(bankAccount);
    }

    /**
     * Deletes all Transactions from transaction queue and from database.
     */
    public void reset() {
        this.transactionMangr.reset();
    }

    /**
     * Calculates the current balance of the bank account. All transactions from the database belonging to the
     * corresponding bank account are evaluated
     *
     * @param bankAccount
     *
     * @return current balance of bankAccount
     */
    public double calculateBalance(BankAccount bankAccount) {
        return this.auditTrail.calculateBalance(bankAccount);
    }

    public List<Transaction> getAccountStatement(BankAccount bankAccount, LocalDateTime begin, LocalDateTime end) {
        return this.auditTrail.getAccountStatement(bankAccount, begin, end);
    }

}
