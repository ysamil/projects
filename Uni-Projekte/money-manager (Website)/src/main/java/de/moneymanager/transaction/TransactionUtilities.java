package de.moneymanager.transaction;

import java.util.regex.Pattern;

public final class TransactionUtilities {

    private static final Pattern IBAN_REGEX = Pattern.compile("DE\\d{20}");

    private TransactionUtilities() {
    }

    public static boolean checkIban(String iban) {
        return iban != null && IBAN_REGEX.matcher(iban).matches();
    }

}
