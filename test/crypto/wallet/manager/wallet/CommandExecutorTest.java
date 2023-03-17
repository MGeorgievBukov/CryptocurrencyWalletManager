package crypto.wallet.manager.wallet;

import crypto.wallet.manager.account.Account;
import crypto.wallet.manager.command.Command;
import crypto.wallet.manager.command.CommandExecutor;
import crypto.wallet.manager.database.Accounts;
import crypto.wallet.manager.database.CryptoCoinsInformation;
import crypto.wallet.manager.exceptions.AccountAlreadyExistsException;
import crypto.wallet.manager.exceptions.AccountDoesNotExistException;
import crypto.wallet.manager.exceptions.AccountIsAlreadyLoggedInException;
import crypto.wallet.manager.exceptions.CryptoCoinDoesNotExistException;
import crypto.wallet.manager.exceptions.InsufficientBalanceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.channels.SelectionKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommandExecutorTest {

    private static final String INVALID_INPUT_ARGUMENTS = "This is not the correct way to use this command";

    public BasicWallet basicWalletMock = mock(BasicWallet.class);

    public static SelectionKey key = mock(SelectionKey.class);

    public static CryptoCoinsInformation cryptoCoinsInformation = mock(CryptoCoinsInformation.class);

    public static Accounts accounts = mock(Accounts.class);

    public Account account;

    public static CommandExecutor commandExecutor;

    @BeforeEach
    public void setup() {
        commandExecutor = new CommandExecutor(accounts, cryptoCoinsInformation);
        account = new Account("misho", "123");
        key.attach(account);
    }

    @Test
    public void testExecuteLoginAccountDoesNotExist() throws AccountDoesNotExistException, AccountIsAlreadyLoggedInException {
        key.attach(null);
        when(accounts.login(anyString(), anyString())).thenThrow(AccountDoesNotExistException.class);

        assertEquals("Wrong username or password.", commandExecutor
                .execute(Command.newCommand("login misho 123"), key), "Login with invalid credentials not returning correct string.");
    }

    @Test
    public void testExecuteLoginTooManyArguments() throws AccountDoesNotExistException, AccountIsAlreadyLoggedInException {
        key.attach(null);
        when(accounts.login("misho", "123")).thenThrow(AccountDoesNotExistException.class);

        assertEquals(INVALID_INPUT_ARGUMENTS, commandExecutor
                .execute(Command.newCommand("login misho 123 123"), key), "Login with invalid credentials not returning correct string.");
    }

    @Test
    public void testExecuteLoginAccountAlreadyLoggedIn() {
        assertEquals("You are already logged in.", commandExecutor
                .execute(Command.newCommand("login misho 123"), key), "Login when already logged in mustn't be possible.");
    }

    @Test
    public void testExecuteLoginAccountSuccessfulLogin() throws AccountDoesNotExistException, AccountIsAlreadyLoggedInException {
        key.attach(null);
        when(accounts.login(anyString(), anyString())).thenReturn(new Account("misho", "123"));

        assertEquals("Login successful", commandExecutor
                .execute(Command.newCommand("login misho 123"), key), "Login when already logged in mustn't be possible.");
        assertEquals(new Account("misho", "123"), key.attachment(), "Logged in account should be attached to key.");
    }

    @Test
    public void testExecuteRegisterAccountSuccessfulRegister() throws AccountAlreadyExistsException {
        key.attach(null);
        doNothing().when(accounts).createAccount(new Account("misho", "123"));

        assertEquals("Register successful", commandExecutor.execute(Command.newCommand("register misho 123"), key));
    }

    @Test
    public void testExecuteRegisterAccountAlreadyExists() throws AccountAlreadyExistsException {
        key.attach(null);
        doThrow(AccountAlreadyExistsException.class).when(accounts).createAccount(new Account("misho", "123"));

        assertEquals("Account already exists", commandExecutor.execute(Command.newCommand("register misho 123"), key));
    }

    @Test
    public void testExecuteRegisterAlreadyLoggedIn() {
        assertEquals("Cannot make a new account while already logged in.", commandExecutor.execute(Command.newCommand("register misho 123"), key));
    }

    @Test
    public void testExecuteRegisterTooManyArguments() {
        key.attach(null);

        assertEquals(INVALID_INPUT_ARGUMENTS, commandExecutor.execute(Command.newCommand("register misho 123 123"), key));
    }

    @Test
    public void testExecuteDepositNotLoggedIn() {
        key.attach(null);

        assertEquals("Log in first or create a new account if you don't have one.",
                commandExecutor.execute(Command.newCommand("deposit 50.0"), key), "Depositing while not logged in mustn't be possible");
    }

    @Test
    public void testExecuteDepositTooManyArguments() {
        assertEquals(INVALID_INPUT_ARGUMENTS,
                commandExecutor.execute(Command.newCommand("deposit 50.0 50"), key), "Argument reading is not correct.");
    }

    @Test
    public void testExecuteDepositSuccessful() {
        when(accounts.getWalletOf(account)).thenReturn(basicWalletMock);
        doNothing().when(basicWalletMock).deposit(anyDouble());

        assertEquals("Transaction completed", commandExecutor.execute(Command.newCommand("deposit 50.0"), key), "Depositing money is not working properly.");
    }

    @Test
    public void testExecuteDepositInvalidFormatForMoney() {
        assertEquals("Invalid format for an amount of money.",
                commandExecutor.execute(Command.newCommand("deposit test"), key), "Exception must be thrown when argument is not parsable.");
    }

    @Test
    public void testExecuteDepositAmountIsNegative() {
        when(accounts.getWalletOf(account)).thenReturn(basicWalletMock);
        doThrow(IllegalArgumentException.class).when(basicWalletMock).deposit(anyDouble());

        assertEquals("Amount cannot be negative.", commandExecutor.execute(Command.newCommand("deposit -50.0"), key), "Negative amount mustn't e acceptable.");
    }

    @Test
    public void testExecuteListOfferingsNotLoggedIn() {
        key.attach(null);

        assertEquals("Log in first or create a new account if you don't have one.",
                commandExecutor.execute(Command.newCommand("list-cryptos"), key), "Must be logged in to see crypto listings.");
    }

    @Test
    public void testExecuteListOfferingsLoggedIn() {
        when(cryptoCoinsInformation.listOfferings()).thenReturn("Test output");

        assertEquals("Test output",
                commandExecutor.execute(Command.newCommand("list-cryptos"), key), "Returned string is not valid.");
    }

    @Test
    public void testExecuteBuyCryptoNotLoggedIn() {
        key.attach(null);

        assertEquals("Log in first or create a new account if you don't have one.",
                commandExecutor.execute(Command.newCommand("buy-crypto"), key), "Must be logged in to buy crypto.");
    }

    @Test
    public void testExecuteBuyCryptoTooManyArguments() {
        assertEquals(INVALID_INPUT_ARGUMENTS,
                commandExecutor.execute(Command.newCommand("buy-crypto 1 2 3 4"), key), "Argument parsing is not valid.");
    }

    @Test
    public void testExecuteBuyCryptoNotParsableArgument() {
        assertEquals("Invalid format for an amount of money.",
                commandExecutor.execute(Command.newCommand("buy-crypto BTC test"), key), "Argument parsing is not valid.");
    }

    @Test
    public void testExecuteBuyCryptoNegativeArgument() throws InsufficientBalanceException, CryptoCoinDoesNotExistException {
        when(accounts.getWalletOf(account)).thenReturn(basicWalletMock);
        doThrow(IllegalArgumentException.class).when(basicWalletMock).buyCryptoCoin(anyDouble(), anyString(), eq(cryptoCoinsInformation));

        assertEquals("Amount cannot be negative.",
                commandExecutor.execute(Command.newCommand("buy-crypto BTC -50"), key), "Transaction with negative amount mustn't be possible.");
    }

    @Test
    public void testExecuteBuyCryptoInsufficientBalance() throws InsufficientBalanceException, CryptoCoinDoesNotExistException {
        when(accounts.getWalletOf(account)).thenReturn(basicWalletMock);
        doThrow(InsufficientBalanceException.class).when(basicWalletMock).buyCryptoCoin(anyDouble(), anyString(), eq(cryptoCoinsInformation));

        assertEquals("There is not enough available balance in your wallet.",
                commandExecutor.execute(Command.newCommand("buy-crypto BTC 50"), key), "Transaction with surpassing amount mustn't be possible.");
    }

    @Test
    public void testExecuteBuyCryptoOfferingCodeDoesNotExist() throws InsufficientBalanceException, CryptoCoinDoesNotExistException {
        when(accounts.getWalletOf(account)).thenReturn(basicWalletMock);
        doThrow(CryptoCoinDoesNotExistException.class).when(basicWalletMock).buyCryptoCoin(anyDouble(), anyString(), eq(cryptoCoinsInformation));

        assertEquals("No crypto coin exists with this id.",
                commandExecutor.execute(Command.newCommand("buy-crypto BTC 50"), key), "Buying non existing coin should throw exception.");
    }

    @Test
    public void testExecuteBuyCryptoSuccessful() throws InsufficientBalanceException, CryptoCoinDoesNotExistException {
        when(accounts.getWalletOf(account)).thenReturn(basicWalletMock);
        doNothing().when(basicWalletMock).buyCryptoCoin(anyDouble(), anyString(), eq(cryptoCoinsInformation));

        assertEquals("Transaction completed",
                commandExecutor.execute(Command.newCommand("buy-crypto BTC 50"), key), "Buying crypto is not working properly.");
    }

    @Test
    public void testExecuteSellCryptoNotLoggedIn() {
        key.attach(null);

        assertEquals("Log in first or create a new account if you don't have one.",
                commandExecutor.execute(Command.newCommand("sell-crypto"), key), "Must be logged in to sell crypto.");
    }

    @Test
    public void testExecuteSellCryptoTooManyArguments() {
        assertEquals(INVALID_INPUT_ARGUMENTS,
                commandExecutor.execute(Command.newCommand("sell-crypto 1 2 3 4"), key), "Argument parsing is not valid.");
    }

    @Test
    public void testExecuteSellCryptoOfferingCodeDoesNotExist() throws CryptoCoinDoesNotExistException {
        when(accounts.getWalletOf(account)).thenReturn(basicWalletMock);
        doThrow(CryptoCoinDoesNotExistException.class).when(basicWalletMock).sellCryptoCoin(anyString(), eq(cryptoCoinsInformation));

        assertEquals("No crypto coin exists with this id.",
                commandExecutor.execute(Command.newCommand("sell-crypto BTC"), key), "Selling non existing coin should throw exception.");
    }

    @Test
    public void testExecuteSellCryptoSuccessful() throws CryptoCoinDoesNotExistException {
        when(accounts.getWalletOf(account)).thenReturn(basicWalletMock);
        doNothing().when(basicWalletMock).sellCryptoCoin(anyString(), eq(cryptoCoinsInformation));

        assertEquals("Transaction completed",
                commandExecutor.execute(Command.newCommand("sell-crypto BTC"), key), "Selling is not working properly.");
    }

    @Test
    public void testWalletInformationNotLoggedIn() {
        key.attach(null);

        assertEquals("Log in first or create a new account if you don't have one.",
                commandExecutor.execute(Command.newCommand("get-wallet-information"), key), "Must be logged in to get wallet information.");
    }

    @Test
    public void testWalletInformationSuccessful() {
        when(accounts.getWalletOf(account)).thenReturn(basicWalletMock);
        when(basicWalletMock.walletInformation()).thenReturn("Test message");

        assertEquals("Test message",
                commandExecutor.execute(Command.newCommand("get-wallet-information"), key), "wallet information is not displaying properly.");
    }

    @Test
    public void testGetWalletInvestmentInformationNotLoggedIn() {
        key.attach(null);

        assertEquals("Log in first or create a new account if you don't have one.",
                commandExecutor.execute(Command.newCommand("get-wallet-investment-information"), key), "Must be logged in to get wallet investment information.");
    }

    @Test
    public void testWalletInvestmentInformationSuccessful() throws CryptoCoinDoesNotExistException {
        when(accounts.getWalletOf(account)).thenReturn(basicWalletMock);
        when(basicWalletMock.walletInvestmentInformation(cryptoCoinsInformation)).thenReturn("Test message");

        assertEquals("Test message",
                commandExecutor.execute(Command.newCommand("get-wallet-investment-information"), key), "wallet investment information is not displaying properly.");
    }

    @Test
    public void testExecuteUnknownCommand() {
        assertEquals("Unknown command", commandExecutor.execute(Command.newCommand("testCommand"), key), "When writing invalid command valid message is not returned");
    }

    @Test
    public void testExecuteDisconnectNotLoggedIn() {
        key.attach(null);

        assertEquals("Log in first or create a new account if you don't have one.",
                commandExecutor.execute(Command.newCommand("disconnect"), key), "Must be logged in to be able to disconnect.");
    }

    @Test
    public void testExecuteDisconnect() {
        assertEquals("disconnected",
                commandExecutor.execute(Command.newCommand("disconnect"), key), "Disconnecting is not working properly.");
    }
}