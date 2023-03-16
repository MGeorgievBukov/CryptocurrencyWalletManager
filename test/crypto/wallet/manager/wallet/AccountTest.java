package crypto.wallet.manager.wallet;

import crypto.wallet.manager.account.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccountTest {

    public Account account;

    @BeforeEach
    public void setup() {
        account = new Account("Testname", "Testpassword");
    }

    @Test
    public void testPasswordMatchMatchingPassword() {
        assertTrue(account.passwordMatch("Testpassword"), "Password hashing does not work properly.");
    }

    @Test
    public void testPasswordMatchNotMatchingPassword() {
        assertFalse(account.passwordMatch("wrongpassword"), "Password hashing does not work properly.");
    }

    @Test
    public void testPasswordMatchPasswordIsNull() {
        assertThrows(IllegalArgumentException.class, () -> account.passwordMatch(null), "An exception is expected with null as an argument");
    }
}
