package de.moneymanager.accounts;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankAccountRepository extends CrudRepository<BankAccount, Long> {

    Optional<BankAccount> findByIban(String iban);

}
