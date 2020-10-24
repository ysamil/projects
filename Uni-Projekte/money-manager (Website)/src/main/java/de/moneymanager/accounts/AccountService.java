package de.moneymanager.accounts;

import java.util.List;
import java.util.Optional;

public interface AccountService {

    // UserAccounts

    /**
     * Creates a new {@link UserAccount} and saves it in {@link UserAccountRepository}. The new {@link UserAccount} will
     * have an automatically generated id and password.
     *
     * @param name  The name of the new {@link UserAccount}.
     * @param email The email of the new {@link UserAccount}.
     */
    void createUserAccount(String name, String email);

    /**
     * Creates a new {@link UserAccount} and saves it in {@link UserAccountRepository}. The new {@link UserAccount} will
     * have an automatically generated id and password.
     *
     * @param name        The name of the new {@link UserAccount}.
     * @param email       The email of the new {@link UserAccount}.
     * @param bankAccount The {@link BankAccount} connected with the new {@link UserAccount}.
     */
    void createUserAccount(String name, String email, BankAccount bankAccount);

    /**
     * @return A list containing all {@link UserAccount} saved in {@link UserAccountRepository}.
     */
    List<UserAccount> getUserAccounts();

    /**
     * @param id The id of the {@link UserAccount}.
     *
     * @return An {@link Optional<UserAccount>} which contains a {@link UserAccount} with the given id if it exists.
     */
    Optional<UserAccount> getUserAccountById(Long id);

    /**
     * @param email The email of the {@link UserAccount}.
     *
     * @return An {@link Optional<UserAccount>} which contains a {@link UserAccount} with the given email if it exists.
     */
    Optional<UserAccount> getUserAccountByEmail(String email);

    /**
     * Sets the password of a {@link UserAccount} to the given String.
     *
     * @param userAccount The {@link UserAccount} whose password is going to be changed.
     * @param password    The new password.
     */
    void setUserAccountPassword(UserAccount userAccount, String password);

    void setUserAccountAdmin(UserAccount userAccount, boolean isAdmin);

    /**
     * Sets the enabled status of an {@link UserAccount} to the given boolean value.
     *
     * @param userAccount The {@link UserAccount} to enable or disable.
     * @param enable      True if the {@link UserAccount} should be enable or false if it should be disabled.
     */
    void enableAccount(UserAccount userAccount, Boolean enable);

    // BankAccounts

    /**
     * Creates a new {@link BankAccount} and saves it in {@link BankAccountRepository}. The new {@link BankAccount} will
     * have an automatically generated IBAN.
     *
     * @param name The name of the new {@link BankAccount}.
     */
    void createBankAccount(String name);

    /**
     * @return A list containing all {@link BankAccount} saved in {@link BankAccountRepository}.
     */
    List<BankAccount> getBankAccounts();

    /**
     * @param id the id of the {@link BankAccount}
     *
     * @return An {@link Optional<BankAccount>} which contains a {@link BankAccount} with the given id if it exists.
     */
    Optional<BankAccount> getBankAccountById(long id);

    /**
     * @param iban the iban of the {@link BankAccount}.
     *
     * @return An {@link Optional<BankAccount>} which contains a {@link BankAccount} with the given iban if it exists.
     */
    Optional<BankAccount> getBankAccountByIban(String iban);

    /**
     * Sets the balance of a given {@link BankAccount} to the given value.
     *
     * @param bankAccount The {@link BankAccount} whose balance should be set.
     * @param balance     The new balance.
     */
    void setBankAccountBalance(BankAccount bankAccount, Long balance);

    void setBankAccountName(BankAccount bankAccount, String name);

    /**
     * Sets the enabled status of an {@link BankAccount} to the given boolean value.
     *
     * @param bankAccount The {@link BankAccount} to enable or disable.
     * @param enable      True if the {@link BankAccount} should be enable or false if it should be disabled.
     */
    void enableAccount(BankAccount bankAccount, Boolean enable);

    /**
     * Returns the unlimited {@link BankAccount} if it exists. Otherwise creates a new {@link BankAccount} and returns
     * the new {@link BankAccount}.
     *
     * @return The unlimited {@link BankAccount}
     */
    BankAccount getUnlimitedBankAccount();

    /**
     * Deletes all entries of {@link UserAccountRepository} and {@link BankAccountRepository}.
     */
    void reset();

}
