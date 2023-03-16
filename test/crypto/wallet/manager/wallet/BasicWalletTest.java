package crypto.wallet.manager.wallet;

import crypto.wallet.manager.database.CryptoCoinsInformation;
import crypto.wallet.manager.dto.CryptoCoin;
import crypto.wallet.manager.exceptions.CryptoCoinDoesNotExistException;
import crypto.wallet.manager.exceptions.InsufficientBalanceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BasicWalletTest {

    public CryptoCoinsInformation cryptoCoinsInformation = mock(CryptoCoinsInformation.class);

    public static BasicWallet basicWallet;

    @BeforeEach
    public void setup() {
        basicWallet = new BasicWallet();
    }

    @Test
    public void testDepositAmountIsLessThanZero() {
        assertThrows(IllegalArgumentException.class, () -> basicWallet.deposit(-4), "Expected IllegalArgumentException but was not thrown.");
    }

    @Test
    public void testDepositAmountIsFifty() {
        basicWallet.deposit(50.0);
        assertEquals(50.0, basicWallet.getAmountAvailable(), "Depositing money does not work properly.");
    }

    @Test
    public void testDepositFirstAmountTenThenAmountIsFifty() {
        basicWallet.deposit(10.0);
        basicWallet.deposit(50.0);
        assertEquals(60.0, basicWallet.getAmountAvailable(), "Depositing money does not work properly when multiple transactions occur.");
    }

    @Test
    public void testBuyCryptoCoinAmountIsLessThanZero() {
        assertThrows(IllegalArgumentException.class, () -> basicWallet.buyCryptoCoin(-4, "test", cryptoCoinsInformation), "Cannot buy cryptoCoin with negative amount.");
    }

    @Test
    public void testBuyCryptoCoinOfferingCodeIsNull() {
        assertThrows(IllegalArgumentException.class, () -> basicWallet.buyCryptoCoin(-10, null, cryptoCoinsInformation), "offeringCode cannot be null.");
    }

    @Test
    public void testBuyCryptoCoinAmountIsGreaterThanAvailableAmount() {
        assertThrows(InsufficientBalanceException.class, () -> basicWallet.buyCryptoCoin(10, "test", cryptoCoinsInformation), "Amount cannot be greater than the available funds.");
    }

    @Test
    public void testBuyCryptoCoinForFiftyDollars() throws CryptoCoinDoesNotExistException, InsufficientBalanceException {
        when(cryptoCoinsInformation.contains("BTC")).thenReturn(new CryptoCoin("BTC", "Bitcoin", 10.0, 1));

        basicWallet.deposit(50.0);
        basicWallet.buyCryptoCoin(50.0, "BTC", cryptoCoinsInformation);

        double amountOfCoinsBought = basicWallet.getCryptoCoins().get("BTC").keySet().stream()
                .findFirst().get();

        assertEquals(5.0, amountOfCoinsBought, "Buying crypto is not working properly");
        assertEquals(0.0, basicWallet.getAmountAvailable(), "When buying crypto money should be withdrawn from wallet");
    }

    @Test
    public void testWalletInformationNoOperationsDone() {
        String expectedResult = "Amount available: 0.0"
                + System.lineSeparator() + "Current investments:" + System.lineSeparator();

        assertEquals(expectedResult, basicWallet.walletInformation(), "Wallet information printing with no operations is invalid.");
    }

    @Test
    public void testWalletInformationDepositMoneyAndBuyCrypto() throws CryptoCoinDoesNotExistException, InsufficientBalanceException {
        when(cryptoCoinsInformation.contains("BTC")).thenReturn(new CryptoCoin("BTC", "Bitcoin", 10.0, 1));

        basicWallet.deposit(50.0);
        basicWallet.buyCryptoCoin(40.0, "BTC", cryptoCoinsInformation);

        when(cryptoCoinsInformation.contains("ADA")).thenReturn(new CryptoCoin("ADA", "Cardano", 5.0, 1));

        basicWallet.deposit(60.0);
        basicWallet.buyCryptoCoin(50.0, "ADA", cryptoCoinsInformation);

        String expectedResult = "Amount available: 20.0"
                + System.lineSeparator() + "Current investments:" + System.lineSeparator() +
                "BTC - 4.0 coins" + System.lineSeparator() + "ADA - 10.0 coins" + System.lineSeparator();

        assertEquals(expectedResult, basicWallet.walletInformation(), "Wallet printing information is not working right.");
    }

    @Test
    public void testWalletInformationDepositMoneyAndBuyCryptoOneCoinAtDifferentPrices() throws CryptoCoinDoesNotExistException, InsufficientBalanceException {
        when(cryptoCoinsInformation.contains("BTC")).thenReturn(new CryptoCoin("BTC", "Bitcoin", 10.0, 1));

        basicWallet.deposit(50.0);
        basicWallet.buyCryptoCoin(40.0, "BTC", cryptoCoinsInformation);

        when(cryptoCoinsInformation.contains("BTC")).thenReturn(new CryptoCoin("BTC", "Bitcoin", 5.0, 1));

        basicWallet.deposit(60.0);
        basicWallet.buyCryptoCoin(50.0, "BTC", cryptoCoinsInformation);

        String expectedResult = "Amount available: 20.0"
                + System.lineSeparator() + "Current investments:" + System.lineSeparator() +
                "BTC - 14.0 coins" + System.lineSeparator();

        assertEquals(expectedResult, basicWallet.walletInformation(), "Wallet printing information is not working right.");
    }

    @Test
    public void testWalletInvestmentInformationNoInvestments() throws CryptoCoinDoesNotExistException {
        assertEquals("Currently there aren't any investments.", basicWallet.walletInvestmentInformation(cryptoCoinsInformation), "Investment information with no investments is not valid.");
    }

    @Test
    public void testWalletInvestmentInformationOneCoinNoProfit() throws CryptoCoinDoesNotExistException, InsufficientBalanceException {
        when(cryptoCoinsInformation.contains("BTC")).thenReturn(new CryptoCoin("BTC", "Bitcoin", 10.0, 1));

        basicWallet.deposit(50.0);
        basicWallet.buyCryptoCoin(50.0, "BTC", cryptoCoinsInformation);
        assertEquals("BTC - +0.0" + System.lineSeparator(), basicWallet.walletInvestmentInformation(cryptoCoinsInformation), "Investment information with no investments is not valid.");
    }

    @Test
    public void testWalletInvestmentInformationOneCoinWithProfit() throws CryptoCoinDoesNotExistException, InsufficientBalanceException {
        when(cryptoCoinsInformation.contains("BTC")).thenReturn(new CryptoCoin("BTC", "Bitcoin", 10.0, 1));

        basicWallet.deposit(50.0);
        basicWallet.buyCryptoCoin(50.0, "BTC", cryptoCoinsInformation);

        when(cryptoCoinsInformation.contains("BTC")).thenReturn(new CryptoCoin("BTC", "Bitcoin", 20.0, 1));

        assertEquals("BTC - +50.0" + System.lineSeparator(), basicWallet.walletInvestmentInformation(cryptoCoinsInformation), "Investment information with no investments is not valid.");
    }

    @Test
    public void testWalletInvestmentInformationOneCoinLoss() throws CryptoCoinDoesNotExistException, InsufficientBalanceException {
        when(cryptoCoinsInformation.contains("BTC")).thenReturn(new CryptoCoin("BTC", "Bitcoin", 10.0, 1));

        basicWallet.deposit(50.0);
        basicWallet.buyCryptoCoin(50.0, "BTC", cryptoCoinsInformation);

        when(cryptoCoinsInformation.contains("BTC")).thenReturn(new CryptoCoin("BTC", "Bitcoin", 5.0, 1));

        assertEquals("BTC - -25.0" + System.lineSeparator(), basicWallet.walletInvestmentInformation(cryptoCoinsInformation), "Investment information with no investments is not valid.");
    }

    @Test
    public void testWalletInvestmentInformationTwoCoinsOneProfitOneLoss() throws CryptoCoinDoesNotExistException, InsufficientBalanceException {
        when(cryptoCoinsInformation.contains("BTC")).thenReturn(new CryptoCoin("BTC", "Bitcoin", 10.0, 1));
        when(cryptoCoinsInformation.contains("ADA")).thenReturn(new CryptoCoin("ADA", "Cardano", 5.0, 1));

        basicWallet.deposit(40.0);
        basicWallet.buyCryptoCoin(20.0, "BTC", cryptoCoinsInformation);
        basicWallet.buyCryptoCoin(20.0, "ADA", cryptoCoinsInformation);

        when(cryptoCoinsInformation.contains("BTC")).thenReturn(new CryptoCoin("BTC", "Bitcoin", 20.0, 1));
        when(cryptoCoinsInformation.contains("ADA")).thenReturn(new CryptoCoin("ADA", "Cardano", 2.5, 1));

        String expectedResult = "BTC - +20.0" + System.lineSeparator() + "ADA - -10.0" + System.lineSeparator();

        assertEquals(expectedResult, basicWallet.walletInvestmentInformation(cryptoCoinsInformation), "Investment information with profit and loss is invalid.");
    }

    @Test
    public void testSellCryptoCoinCryptoCoinIsNull() {
        assertThrows(IllegalArgumentException.class, () -> basicWallet.sellCryptoCoin(null, cryptoCoinsInformation), "Cannot sell crypto with null argument.");
    }

    @Test
    public void testSellCryptoCoinCryptoCoinDoesNotExist() throws CryptoCoinDoesNotExistException {
        when(cryptoCoinsInformation.contains("BTC")).thenThrow(CryptoCoinDoesNotExistException.class);
        assertThrows(CryptoCoinDoesNotExistException.class, () -> basicWallet.sellCryptoCoin("BTC", cryptoCoinsInformation), "Cannot sell crypto with null argument.");
    }

    @Test
    public void testSellCryptoCoinCryptoCoinExists() throws CryptoCoinDoesNotExistException, InsufficientBalanceException {
        when(cryptoCoinsInformation.contains("BTC")).thenReturn(new CryptoCoin("BTC", "Bitcoin", 10.0, 1));
        basicWallet.deposit(50.0);
        basicWallet.buyCryptoCoin(50.0, "BTC", cryptoCoinsInformation);

        when(cryptoCoinsInformation.contains("BTC")).thenReturn(new CryptoCoin("BTC", "Bitcoin", 30.0, 1));
        basicWallet.sellCryptoCoin("BTC", cryptoCoinsInformation);

        assertEquals(150.0, basicWallet.getAmountAvailable(), "Conversion from crypto to money is not working properly.");
        assertNull(basicWallet.getCryptoCoins().get("BTC"), "Method should sell all crypto");
    }
}
