package de.moneymanager.accounts;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter(AccessLevel.PACKAGE)
public class BankAccount extends Account {

    @Column(unique = true, nullable = false)
    private String iban;
    private Long   balance = 0L;

    @OneToMany(mappedBy = "bankAccount")
    private Set<UserAccount> userAccounts = new HashSet<>();

    private void addUserAccount(UserAccount userAccount) {
        userAccount.setBankAccount(this);
        this.userAccounts.add(userAccount);
    }

    private void removeUserAccount(UserAccount userAccount) {
        userAccount.setBankAccount(null);
        this.userAccounts.remove(userAccount);
    }

    public BankAccount() {
    }

    public BankAccount(String name, String iban) {
        this.setName(name);
        this.iban = iban;
    }

    @Override
    public String toString() {
        return "BankAccount{" +
               "id='" + this.getId() + '\'' +
               ", name=" + this.getName() +
               ", iban=" + this.iban +
               ", balance=" + this.balance +
               ", enabled=" + this.getEnable() +
               "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        BankAccount that = (BankAccount) o;
        return Objects.equals(this.iban, that.iban);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.iban);
    }

}
