package de.moneymanager.auth;

import java.security.SecureRandom;

public class AuthUtils {

    private static final int    PASSWORD_LENGTH     = 10;
    private static final String PASSWORD_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

    /**
     * Generates a random password withe a length of {@link AuthUtils#PASSWORD_LENGTH}. The password is generated out of
     * characters contained in {@link AuthUtils#PASSWORD_CHARACTERS}.
     *
     * @return A random password of length {@link AuthUtils#PASSWORD_LENGTH}.
     */
    public static String generatePassword() {
        SecureRandom  random = new SecureRandom();
        StringBuilder sb     = new StringBuilder();

        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int randomIndex = random.nextInt(PASSWORD_CHARACTERS.length());
            sb.append(PASSWORD_CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }

}
