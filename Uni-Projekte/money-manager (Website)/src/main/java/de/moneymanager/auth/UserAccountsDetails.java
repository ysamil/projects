package de.moneymanager.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class UserAccountsDetails implements UserDetails {

    private final String  username;
    private final String  password;
    private final boolean enabled;
    private final boolean isAdmin;

    public UserAccountsDetails(String username, String password, boolean enabled, boolean isAdmin) {
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.isAdmin = isAdmin;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        if (this.isAdmin) {
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return grantedAuthorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public String toString() {
        return "UserAccountsDetails{" +
               "username='" + this.username + '\'' +
               ", password='" + this.password + '\'' +
               ", enabled=" + this.enabled +
               ", isAdmin=" + this.isAdmin +
               '}';
    }

}
