package crypto.wallet.manager.wallet;

import crypto.wallet.manager.database.CryptoCoinsInformation;
import crypto.wallet.manager.dto.CryptoCoin;
import crypto.wallet.manager.exceptions.CryptoCoinDoesNotExistException;
import crypto.wallet.manager.exceptions.InsufficientBalanceException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BasicWallet implements Wallet {

    public static final double ZERO_BALANCE = 0.0;

    private double amountAvailable;

    //offeringCode, amountOfCoin, priceBoughtAt
    private final Map<String, Map<Double, Double>> cryptoCoins;

    public BasicWallet() {
        this.cryptoCoins = new HashMap<>();
        amountAvailable = ZERO_BALANCE;
    }

    public double getAmountAvailable() {
        return amountAvailable;
    }

    public Map<String, Map<Double, Double>> getCryptoCoins() {
        return Collections.unmodifiableMap(cryptoCoins);
    }

    public void deposit(double amount) throws IllegalArgumentException {
        if (amount < ZERO_BALANCE) {
            throw new IllegalArgumentException("amount cannot be a negative number");
        }

        amountAvailable += amount;
    }

    public void buyCryptoCoin(double amount, String offeringCode, CryptoCoinsInformation cryptoCoinsInformation)
            throws CryptoCoinDoesNotExistException, InsufficientBalanceException {
        if (amount < ZERO_BALANCE) {
            throw new IllegalArgumentException("amount cannot be a negative number");
        }

        if (offeringCode == null) {
            throw new IllegalArgumentException("offeringCode cannot be null");
        }

        if (amount > amountAvailable) {
            throw new InsufficientBalanceException("Not enough available amount to pay for transaction.");
        }

        if (!cryptoCoins.containsKey(offeringCode)) {
            cryptoCoins.put(offeringCode, new HashMap<>());
        }

        CryptoCoin cryptoCoin = cryptoCoinsInformation.contains(offeringCode);
        cryptoCoins.get(offeringCode).put(amount / cryptoCoin.getPriceUSD(), cryptoCoin.getPriceUSD());
        amountAvailable -= amount;
    }

    public String walletInformation() {
        StringBuilder result = new StringBuilder("Amount available: " + this.amountAvailable
                + System.lineSeparator() + "Current investments:" + System.lineSeparator());

        for (var set : cryptoCoins.entrySet()) {
            result.append(set.getKey()).append(" - ")
                    .append(accumulateWholeAmountOfACoin(set.getValue().keySet()))
                    .append(" coins").append(System.lineSeparator());
        }

        return result.toString();
    }

    public String walletInvestmentInformation(CryptoCoinsInformation cryptoCoinsInformation)
            throws CryptoCoinDoesNotExistException {
        StringBuilder result = new StringBuilder();
        for (var set : cryptoCoins.entrySet()) {
            CryptoCoin cryptoCoin = cryptoCoinsInformation.contains(set.getKey());

            double amountInvestedInACoin = accumulateAmountInvestedInACoin(set.getValue());
            double amountOfCoin = accumulateWholeAmountOfACoin(set.getValue().keySet());
            double priceMargin = amountOfCoin * cryptoCoin.getPriceUSD() - amountInvestedInACoin;

            String coinInformation;
            if (priceMargin >= ZERO_BALANCE) {
                coinInformation = String.format(set.getKey() + " - " + "+" + priceMargin);
            } else {
                coinInformation = String.format(set.getKey() + " - " + priceMargin);
            }

            result.append(coinInformation).append(System.lineSeparator());
        }

        if (result.isEmpty()) {
            result.append("Currently there aren't any investments.");
        }

        return result.toString();
    }

    public void sellCryptoCoin(String offeringCode, CryptoCoinsInformation cryptoCoinsInformation)
            throws CryptoCoinDoesNotExistException {
        if (offeringCode == null) {
            throw new IllegalArgumentException("offeringCode cannot be null");
        }

        CryptoCoin cryptoCoin = cryptoCoinsInformation.contains(offeringCode);
        double amountOfCoin = accumulateWholeAmountOfACoin(cryptoCoins.get(offeringCode).keySet());
        cryptoCoins.remove(offeringCode);
        deposit(cryptoCoin.getPriceUSD() * amountOfCoin);
    }

    private double accumulateWholeAmountOfACoin(Set<Double> cryptoCoinAmount) {
        double result = ZERO_BALANCE;

        for (var object : cryptoCoinAmount) {
            result += object;
        }

        return result;
    }

    private double accumulateAmountInvestedInACoin(Map<Double, Double> cryptoCoinInvestmentData) {
        double result = ZERO_BALANCE;

        for (var object : cryptoCoinInvestmentData.entrySet()) {
            result += object.getKey() * object.getValue();
        }

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasicWallet basicWallet = (BasicWallet) o;

        if (Double.compare(basicWallet.amountAvailable, amountAvailable) != 0) return false;
        return cryptoCoins.equals(basicWallet.cryptoCoins);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(amountAvailable);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + cryptoCoins.hashCode();
        return result;
    }
}
