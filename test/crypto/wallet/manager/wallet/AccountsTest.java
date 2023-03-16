package crypto.wallet.manager.wallet;

import crypto.wallet.manager.account.Account;
import crypto.wallet.manager.database.Accounts;
import crypto.wallet.manager.exceptions.AccountAlreadyExistsException;
import crypto.wallet.manager.exceptions.AccountDoesNotExistException;
import crypto.wallet.manager.exceptions.AccountIsAlreadyLoggedInException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class AccountsTest {

    public Accounts accounts;

    public static String accountsPath = "accounts.dat";

    public static String walletsPath = "wallets.dat";

    private static void readFromFiles() {
        Path pathOfWallets = Path.of(walletsPath);

        try (var objectOutputStream = new ObjectOutputStream(Files.newOutputStream(pathOfWallets))) {
            for (int i = 0; i < 2; i++) {
                objectOutputStream.writeObject(new BasicWallet());
                objectOutputStream.flush();
            }
        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while writing to a file", e);
        }

        Path pathOfAccounts = Path.of(accountsPath);

        try (var objectOutputStream = new ObjectOutputStream(Files.newOutputStream(pathOfAccounts))) {
            Account accountOne = new Account("misho", "123");
            objectOutputStream.writeObject(accountOne);
            objectOutputStream.flush();

            Account accountTwo = new Account("toshko", "543");
            objectOutputStream.writeObject(accountTwo);
            objectOutputStream.flush();
        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while writing to a file", e);
        }
    }

    @BeforeAll
    public static void setup() {
        readFromFiles();
    }

    @AfterAll
    public static void teardown() {
        try {
            Files.delete(Path.of(accountsPath));
            Files.delete(Path.of(walletsPath));
        } catch (IOException e) {
            throw new RuntimeException("Deleting of accounts.dat and wallets.dat failed.");
        }
    }

    @BeforeEach
    public void init() {
        accounts = new Accounts(accountsPath, walletsPath);
        accounts.loadData(accountsPath, walletsPath);
    }

    @Test
    public void testInitialization() {
        Map<Account, BasicWallet> expectedResult = new LinkedHashMap<>();
        expectedResult.put(new Account("misho", "123"), new BasicWallet());
        expectedResult.put(new Account("toshko", "543"), new BasicWallet());
        assertEquals(expectedResult, accounts.getAccountsAndWallets(), "Reading from files is not working properly.");
    }

    @Test
    public void testLoginAccountExists() throws AccountDoesNotExistException, AccountIsAlreadyLoggedInException {
        Account expectedAccountToExist = new Account("misho", "123");
        Account receivedAccount = accounts.login("misho", "123");

        assertEquals(expectedAccountToExist, receivedAccount, "Returned account after login is not right");
    }

    @Test
    public void testLoginAccountDoesNotExist() {
        assertThrows(AccountDoesNotExistException.class, () -> accounts.login("testName", "123"), "Account does not exist but no exception is thrown");
    }

    @Test
    public void testLoginAccountIsAlreadyLoggedIn() throws AccountDoesNotExistException, AccountIsAlreadyLoggedInException {
        accounts.login("misho", "123");
        assertThrows(AccountIsAlreadyLoggedInException.class, () -> accounts.login("misho", "123"), "Account is already logged in but no exception is thrown");
    }

    @Test
    public void testCreateAccountDoesNotExist() throws AccountAlreadyExistsException {
        Map<Account, BasicWallet> expectedResult = new LinkedHashMap<>();
        expectedResult.put(new Account("misho", "123"), new BasicWallet());
        expectedResult.put(new Account("toshko", "543"), new BasicWallet());

        Account newAccount = new Account("Todor", "765");
        expectedResult.put(newAccount, new BasicWallet());

        accounts.createAccount(newAccount);
        assertEquals(expectedResult, accounts.getAccountsAndWallets(), "Creating account is not working correctly.");
    }

    @Test
    public void testCreateAccountAlreadyExists() {
        assertThrows(AccountAlreadyExistsException.class, () -> accounts.createAccount(
                new Account("toshko", "895")), "Account already exists but no exception is thrown");
    }

    @Test
    public void testGetWalletOfAccountIsNull() {
        assertThrows(IllegalArgumentException.class, () -> accounts.getWalletOf(null), "null cannot be argument but not exception is thrown");
    }

    @Test
    public void testGetWalletOfAccount() {
        assertEquals(new BasicWallet(), accounts.getWalletOf(new Account("misho")), "Returned wallet is not right");
    }

    @Test
    public void testClose() throws Exception {
        accounts.createAccount(new Account("drago", "123"));
        accounts.close();
        readFromFiles();
        Map<Account, BasicWallet> expectedResult = new LinkedHashMap<>();
        expectedResult.put(new Account("misho", "123"), new BasicWallet());
        expectedResult.put(new Account("toshko", "543"), new BasicWallet());
        expectedResult.put(new Account("drago", "123"), new BasicWallet());
        assertEquals(expectedResult, accounts.getAccountsAndWallets(), "Reading from files is not working properly.");
    }
}
