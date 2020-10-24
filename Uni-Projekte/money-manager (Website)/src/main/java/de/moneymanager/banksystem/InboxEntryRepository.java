package de.moneymanager.banksystem;

import de.moneymanager.accounts.BankAccount;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InboxEntryRepository extends CrudRepository<InboxEntry, Long> {

    Iterable<InboxEntry> findAllByBankAccount(BankAccount bankAccount);

}