package crypto.wallet.manager.database;

import crypto.wallet.manager.dto.CryptoCoin;
import crypto.wallet.manager.exceptions.CryptoCoinDoesNotExistException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CryptoCoinsInformation {

    private static final int LIMIT_COINS = 150;

    private static Set<CryptoCoin> cryptoCoinSet;

    public CryptoCoinsInformation() {
        cryptoCoinSet = new HashSet<>();
    }

    public Set<CryptoCoin> getCryptoCoinSet() {
        return Collections.unmodifiableSet(cryptoCoinSet);
    }

    public void updateCryptoCoinSetInformation(Set<CryptoCoin> newSet) {
        if (newSet == null) {
            throw new IllegalArgumentException("The new set of crypto coins has not been created properly.");
        }

        cryptoCoinSet = newSet.stream()
                .filter(CryptoCoin::isCrypto)
                .limit(LIMIT_COINS)
                .collect(Collectors.toSet());
    }

    public CryptoCoin contains(String offeringCode) throws CryptoCoinDoesNotExistException {
        for (var object : cryptoCoinSet) {
            if (object.getOfferingCode().equals(offeringCode)) {
                return object;
            }
        }

        throw new CryptoCoinDoesNotExistException("A crypto coin with this offeringCode does not exist.");
    }

    public String listOfferings() {
        StringBuilder result = new StringBuilder("Available cryptos:" + System.lineSeparator());
        for (var coin : cryptoCoinSet) {
            result.append(coin.getName()).append(" - ").append(coin.getPriceUSD())
                    .append(" US dollars").append(System.lineSeparator());
        }

        return result.toString();
    }
}
