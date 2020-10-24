package de.moneymanager.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class AuthUtilsTest {

    @Test
    void shouldCreateRandomPassword() {
        String pw1 = AuthUtils.generatePassword();
        String pw2 = AuthUtils.generatePassword();

        assertNotEquals(pw1, pw2);
        assertEquals(10, pw1.length());
        assertEquals(10, pw2.length());
    }

}