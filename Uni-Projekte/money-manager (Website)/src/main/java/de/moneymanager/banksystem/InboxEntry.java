package de.moneymanager.banksystem;

import de.moneymanager.accounts.BankAccount;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity that describes an entry to the inbox.
 */
@Entity
@Getter
@Setter
public class InboxEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long          id;
    @ManyToOne
    private BankAccount   bankAccount;
    private LocalDateTime timeStamp;
    private String        pdfName;

}
