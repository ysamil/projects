package de.moneymanager.transaction;

import de.moneymanager.accounts.BankAccount;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Long> {

    Iterable<Transaction> findAllByDestinationOrSourceOrderByDateDesc(BankAccount destination, BankAccount source);

    @Query("select t from Transaction t where t.date between ?1 and ?2 and (t.destination = ?3 or t.source = ?3) order by t.date desc")
    Iterable<Transaction> findAllByDateAndBankAccount(LocalDateTime start, LocalDateTime end, BankAccount bankAccount);

}
