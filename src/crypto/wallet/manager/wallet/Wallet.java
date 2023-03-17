package crypto.wallet.manager.wallet;

import crypto.wallet.manager.database.CryptoCoinsInformation;
import crypto.wallet.manager.exceptions.CryptoCoinDoesNotExistException;
import crypto.wallet.manager.exceptions.InsufficientBalanceException;

import java.io.Serializable;

public interface Wallet extends Serializable {

    void deposit(double amount);

    void buyCryptoCoin(double amount, String offeringCode, CryptoCoinsInformation cryptoCoinsInformation)
            throws CryptoCoinDoesNotExistException, InsufficientBalanceException;

    String walletInformation();

    String walletInvestmentInformation(CryptoCoinsInformation cryptoCoinsInformation)
            throws CryptoCoinDoesNotExistException;

    void sellCryptoCoin(String offeringCode, CryptoCoinsInformation cryptoCoinsInformation)
            throws CryptoCoinDoesNotExistException;
}
