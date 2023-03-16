package crypto.wallet.manager.exceptions;

public class CryptoCoinDoesNotExistException extends Exception {
    public CryptoCoinDoesNotExistException(String message) {
        super(message);
    }

    public CryptoCoinDoesNotExistException(String message, Throwable e) {
        super(message, e);
    }
}
