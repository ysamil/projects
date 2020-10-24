package de.moneymanager.transaction;

import de.moneymanager.accounts.BankAccount;
import de.moneymanager.accounts.UserAccount;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Transaction {

    public Transaction(String usage, BankAccount source, BankAccount destination, Long amount, UserAccount user) {
        this.usage = usage;
        this.source = source;
        this.destination = destination;
        this.amount = amount;
        this.user = user;
        this.canceled = false;
    }

    public Transaction() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** Usage of transaction. */
    private String usage;

    /** timestamp of execution time of transaction. */
    private LocalDateTime date;

    /** Bank account from which the amount is to be debited. */
    @ManyToOne
    private BankAccount source;

    /** Bank account to which the amount should be credited. */
    @ManyToOne
    private BankAccount destination;

    /** Amount to be transferred. */
    private Long amount;

    /** User account that wants to perform the transaction. */
    @ManyToOne
    private UserAccount user;

    /** Transaction is canceled if account is not sufficiently funded */
    private Boolean canceled;

    @Override
    public String toString() {
        return "Transaction{" +
               "id=" + this.id +
               ", usage='" + this.usage + '\'' +
               ", date=" + this.date +
               ", source=" + this.source +
               ", destination=" + this.destination +
               ", amount=" + this.amount +
               ", user=" + this.user +
               ", canceled=" + this.canceled +
               '}';
    }

}


