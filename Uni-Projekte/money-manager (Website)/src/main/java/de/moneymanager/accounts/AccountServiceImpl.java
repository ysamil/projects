package de.moneymanager.accounts;

import de.moneymanager.auth.AuthUtils;
import de.moneymanager.banksystem.BankSystemService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {

    private final UserAccountRepository userAccountRepository;
    private final BankAccountRepository bankAccountRepository;
    private final PasswordEncoder       passwordEncoder;
    private final BankSystemService     bankSystemService;

    private static final String UNLIMITED_BANK_ACCOUNT_NAME = "Bank";
    private static final String UNLIMITED_BANK_ACCOUNT_IBAN = "DE00000000000000000000";

    public AccountServiceImpl(UserAccountRepository userAccountRepository, BankAccountRepository bankAccountRepository,
                              PasswordEncoder passwordEncoder, @Lazy BankSystemService bankSystemService) {
        this.userAccountRepository = userAccountRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.bankSystemService = bankSystemService;
    }

    /**
     * Generates a random IBAN.
     *
     * @return A randomly generated IBAN.
     */
    private String generateIban() {
        SecureRandom  random        = new SecureRandom();
        StringBuilder iban          = new StringBuilder();
        String        blz           = this.bankSystemService.getBlz();
        StringBuilder accountNumber = new StringBuilder();
        String        checksum      = "00";

        //generate a random accountNumber
        for (int i = 0; i < 10; i++) {
            accountNumber.append(random.nextInt(10));
        }

        //Build a temporarily IBAN using a default checksum. This IBAN is used to calculate the correct checksum.
        iban.append("DE");
        iban.append(checksum);
        iban.append(blz);
        iban.append(accountNumber.toString());

        //Calculate the checksum.
        checksum = String.format("%02d", calculateChecksum(iban.toString()));

        //Replace the default checksum with the calculated one.
        iban.replace(2, 4, checksum);

        return iban.toString();
    }

    /**
     * Checks if the given IBAN has the correct length, checksum, BLZ and "DE" as the first two characters.
     *
     * @param iban The IBAN to check
     *
     * @return true if the given IBAN is valid and false if it isn't.
     */
    public static Boolean checkIban(String iban) {
        //IBAN must be 22 characters long.
        if (iban.length() != 22) {
            return false;
        }

        //Get the two characters at the front.
        String country = iban.substring(0, 2);

        //The first two characters must be "DE".
        if (!country.equals("DE")) {
            return false;
        }

        //Calculate the correct checksum.
        String checksum = String.valueOf(calculateChecksum(iban));

        //Get the checksum of the given IBAN.
        String ibanChecksum = iban.substring(2, 4);

        //The given checksum must be the same as the calculated one.
        return ibanChecksum.equals(checksum);
    }

    /**
     * Calculates for a given IBAN the checksum.
     *
     * @param iban The IBAN of which the checksum should be calculated.
     *
     * @return The checksum of the given IBAN.
     */
    private static int calculateChecksum(String iban) {
        StringBuilder sb = new StringBuilder();

        //Get the BLZ and accountNumber of the given IBAN.
        String ibanStringWithoutChecksum = iban.substring(4);

        //Add 1314 (representing "DE") to the end of the string as well as "00" (the checksum).
        sb.append(ibanStringWithoutChecksum);
        sb.append("131400");

        //Calculate the checksum.
        BigInteger ibanWithoutChecksum = new BigInteger(sb.toString());
        BigInteger x                   = ibanWithoutChecksum.mod(new BigInteger("97"));
        return 98 - x.intValue();
    }

    @Override
    public List<UserAccount> getUserAccounts() {
        List<UserAccount>     users         = new LinkedList<>();
        Iterable<UserAccount> usersIterable = this.userAccountRepository.findAll();
        usersIterable.forEach(users::add);
        return users;
    }

    @Override
    public Optional<UserAccount> getUserAccountById(Long id) {
        return this.userAccountRepository.findById(id);
    }

    @Override
    public Optional<UserAccount> getUserAccountByEmail(String email) {
        return this.userAccountRepository.findByEmail(email);
    }

    @Override
    public void setUserAccountPassword(UserAccount userAccount, String password) {
        // The password should be encrypted before storing it in the db.
        userAccount.setPassword(this.passwordEncoder.encode(password));
        userAccount.setResetPassword(null);
        this.userAccountRepository.save(userAccount);
    }

    @Override
    public void setUserAccountAdmin(UserAccount userAccount, boolean isAdmin) {
        userAccount.setIsAdmin(isAdmin);
        this.userAccountRepository.save(userAccount);
    }

    @Override
    public void enableAccount(UserAccount userAccount, Boolean enable) {
        userAccount.setEnable(enable);
        this.userAccountRepository.save(userAccount);
    }

    @Override
    public List<BankAccount> getBankAccounts() {
        List<BankAccount>     accounts         = new LinkedList<>();
        Iterable<BankAccount> accountsIterable = this.bankAccountRepository.findAll();
        accountsIterable.forEach(accounts::add);
        return accounts;
    }

    @Override
    public Optional<BankAccount> getBankAccountById(long id) {
        return this.bankAccountRepository.findById(id);
    }

    @Override
    public Optional<BankAccount> getBankAccountByIban(String iban) {
        return this.bankAccountRepository.findByIban(iban);
    }

    @Override
    public void createUserAccount(String name, String email) {
        createUserAccount(name, email, null);
    }

    @Override
    public void createUserAccount(String name, String email, BankAccount bankAccount) {
        if (this.userAccountRepository.findByEmail(email).isEmpty()) {
            String password = AuthUtils.generatePassword();
            this.userAccountRepository.save(new UserAccount(name, email, password, bankAccount));
        }
    }

    @Override
    public void createBankAccount(String name) {
        String      iban         = generateIban();
        BankAccount account      = this.bankAccountRepository.save(new BankAccount(name, iban));
        Long        startBalance = this.bankSystemService.getStartBalance();
        setBankAccountBalance(account, startBalance);
    }

    @Override
    public void reset() {
        SecurityContext context     = SecurityContextHolder.getContext();
        UserAccount     userAccount = null;

        if (context != null) {
            Authentication authentication = context.getAuthentication();
            if (authentication != null) {
                String email = authentication.getName();
                userAccount = getUserAccountByEmail(email).orElse(null);
            }

        }

        this.userAccountRepository.deleteAll();
        this.bankAccountRepository.deleteAll();

        if (userAccount != null) {
            this.userAccountRepository.save(userAccount);
        }
    }

    @Override
    public synchronized BankAccount getUnlimitedBankAccount() {
        if (getBankAccountByIban(UNLIMITED_BANK_ACCOUNT_IBAN).isEmpty()) {
            BankAccount unlimited = new BankAccount(UNLIMITED_BANK_ACCOUNT_NAME, UNLIMITED_BANK_ACCOUNT_IBAN);
            return this.bankAccountRepository.save(unlimited);
        } else {
            return getBankAccountByIban(UNLIMITED_BANK_ACCOUNT_IBAN).get();
        }
    }

    @Override
    public void setBankAccountBalance(BankAccount bankAccount, Long balance) {
        bankAccount.setBalance(balance);
        this.bankAccountRepository.save(bankAccount);
    }

    @Override
    public void setBankAccountName(BankAccount bankAccount, String name) {
        bankAccount.setName(name);
        this.bankAccountRepository.save(bankAccount);
    }

    @Override
    public void enableAccount(BankAccount bankAccount, Boolean enable) {
        bankAccount.setEnable(enable);
        this.bankAccountRepository.save(bankAccount);
    }

}
