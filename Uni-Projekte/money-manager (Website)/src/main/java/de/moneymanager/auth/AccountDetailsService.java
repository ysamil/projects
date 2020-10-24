package de.moneymanager.auth;

import de.moneymanager.accounts.AccountService;
import de.moneymanager.accounts.UserAccount;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
class AccountDetailsService implements UserDetailsService {

    private final AccountService  accountService;
    private final PasswordEncoder passwordEncoder;

    AccountDetailsService(AccountService accountService, PasswordEncoder passwordEncoder) {
        this.accountService = accountService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<UserAccount> optionalUserAccount = this.accountService.getUserAccountByEmail(email);

        if (optionalUserAccount.isEmpty()) {
            throw new UsernameNotFoundException("No user with email: " + email);
        }
        UserAccount userAccount = optionalUserAccount.get();

        // Check if an initial password is set or otherwise use the normal one.
        String password =
                userAccount.getResetPassword() != null ? this.passwordEncoder.encode(userAccount.getResetPassword())
                                                       : userAccount.getPassword();

        return new UserAccountsDetails(userAccount.getEmail(),
                                       password,
                                       userAccount.getEnable(),
                                       userAccount.getIsAdmin()
        );
    }

}
