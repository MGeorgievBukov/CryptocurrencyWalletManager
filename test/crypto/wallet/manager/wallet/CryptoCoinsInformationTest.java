package crypto.wallet.manager.wallet;

import crypto.wallet.manager.database.CryptoCoinsInformation;
import crypto.wallet.manager.dto.CryptoCoin;
import crypto.wallet.manager.exceptions.CryptoCoinDoesNotExistException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CryptoCoinsInformationTest {

    public CryptoCoinsInformation cryptoCoinsInformation;

    public static Set<CryptoCoin> cryptoCoins;

    @BeforeAll
    public static void setup() {
        cryptoCoins = new HashSet<>();
        cryptoCoins.add(new CryptoCoin("BTC", "Bitcoin", 50.15, 1));
        cryptoCoins.add(new CryptoCoin("ADA", "Cardano", 35.60, 1));
        cryptoCoins.add(new CryptoCoin("UNI", "Uniswap", 15.40, 1));
    }

    @BeforeEach
    public void init() {
        cryptoCoinsInformation = new CryptoCoinsInformation();
        cryptoCoinsInformation.updateCryptoCoinSetInformation(cryptoCoins);
    }

    @Test
    public void testInitialization() {
        assertEquals(cryptoCoins, cryptoCoinsInformation.getCryptoCoinSet(), "Initialization is not working properly.");
    }

    @Test
    public void testContainsExists() throws CryptoCoinDoesNotExistException {
        CryptoCoin expectedReturn = new CryptoCoin("BTC", "Bitcoin", 50.15, 1);
        assertEquals(expectedReturn, cryptoCoinsInformation.contains("BTC"), "Getting coin by offeringCode is not working properly.");
    }

    @Test
    public void testContainsDoesExists() {
        assertThrows(CryptoCoinDoesNotExistException.class, () -> cryptoCoinsInformation.contains("NET"), "Getting coin by offeringCode that does not exist does not throw exception.");
    }

    @Test
    public void testListOfferings() {
        StringBuilder result = new StringBuilder("Available cryptos:" + System.lineSeparator());
        for (var coin : cryptoCoins) {
            result.append(coin.getName()).append(" - ").append(coin.getPriceUSD())
                    .append(" US dollars").append(System.lineSeparator());
        }

        assertEquals(result.toString(), cryptoCoinsInformation.listOfferings(), "Listing of coins is not working properly.");
    }
}
