package de.moneymanager.accounts;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@Getter
@Setter(AccessLevel.PACKAGE)
public class UserAccount extends Account {

    @Column(unique = true, nullable = false)
    private String      email;
    /** The encrypted password. */
    private String      password;
    /**
     * This is a cleartext password that gets set at the first initialization. It is used to login the first time and
     * than will be set to null.
     */
    private String      resetPassword;
    @ManyToOne
    private BankAccount bankAccount;
    @Column(nullable = false)
    private Boolean     isAdmin = false;

    public UserAccount() {
    }

    public UserAccount(String name, String email, String password) {
        this.setName(name);
        this.email = email;
        this.resetPassword = password;
    }

    public UserAccount(String name, String email, String password, BankAccount bankAccount) {
        this(name, email, password);
        this.bankAccount = bankAccount;
    }

    public UserAccount(String name, String email, String password, BankAccount bankAccount, Boolean isAdmin) {
        this(name, email, password, bankAccount);
        this.isAdmin = isAdmin;
    }

    @Override
    public String toString() {
        return "UserAccount{" +
               "id='" + this.getId() + '\'' +
               ", name='" + this.getName() + '\'' +
               ", email='" + this.email + '\'' +
               ", password='" + this.password + '\'' +
               ", resetPassword='" + this.resetPassword + '\'' +
               ", bankAccount=" + this.bankAccount +
               ", isAdmin=" + this.isAdmin +
               ", enabled='" + this.getEnable() + '\'' +
               "} " + super.toString();
    }

}
