package de.moneymanager.auth;

import de.moneymanager.accounts.AccountService;
import de.moneymanager.accounts.UserAccount;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AccountDetailsServiceTest {

    @Autowired
    AccountService        accountService;
    @Autowired
    AccountDetailsService accountDetailsService;
    @Autowired
    PasswordEncoder       passwordEncoder;

    @AfterEach
    void tearDown() {
        this.accountService.reset();
    }

    @Test
    void shouldLoginCorrectly() {
        this.accountService.reset();

        this.accountService.createUserAccount("User", "user@user.com");
        UserAccount userAccount = this.accountService.getUserAccountByEmail("user@user.com").orElseThrow();

        String resetPassword = userAccount.getResetPassword();

        assertNull(userAccount.getPassword());
        assertNotNull(resetPassword);

        assertThrows(UsernameNotFoundException.class, () -> {
            this.accountDetailsService.loadUserByUsername("null");
        });

        UserDetails userDetails = this.accountDetailsService.loadUserByUsername("user@user.com");

        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        assertFalse(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertTrue(this.passwordEncoder.matches(resetPassword, userDetails.getPassword()));
        assertEquals(userAccount.getEmail(), userDetails.getUsername());

        this.accountService.setUserAccountPassword(userAccount, "password");
        userDetails = this.accountDetailsService.loadUserByUsername("user@user.com");

        assertFalse(this.passwordEncoder.matches(resetPassword, userDetails.getPassword()));
        assertTrue(this.passwordEncoder.matches("password", userDetails.getPassword()));

        this.accountService.setUserAccountPassword(userAccount, "password1");
        userDetails = this.accountDetailsService.loadUserByUsername("user@user.com");

        assertFalse(this.passwordEncoder.matches("password", userDetails.getPassword()));
        assertTrue(this.passwordEncoder.matches("password1", userDetails.getPassword()));

        this.accountService.setUserAccountAdmin(userAccount, true);
        userDetails = this.accountDetailsService.loadUserByUsername("user@user.com");

        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertFalse(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));

    }

}