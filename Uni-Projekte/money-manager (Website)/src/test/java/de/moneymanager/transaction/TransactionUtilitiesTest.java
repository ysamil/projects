package de.moneymanager.transaction;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionUtilitiesTest {

    @Test
    void checkIban() {
        assertTrue(
                TransactionUtilities.checkIban("DE20123141231312112323"),
                "A correct IBAN should get accepted."
                  );

        assertFalse(
                TransactionUtilities.checkIban("DE2012314123131211232"),
                "A short IBAN should not get accepted."
                   );
        
        assertFalse(
                TransactionUtilities.checkIban("GB20123141231312112323"),
                "A foreign IBAN should not get accepted."
                   );
    }

}