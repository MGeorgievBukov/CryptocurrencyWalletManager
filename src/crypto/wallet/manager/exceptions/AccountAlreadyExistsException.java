package crypto.wallet.manager.exceptions;

public class AccountAlreadyExistsException extends Exception {
    public AccountAlreadyExistsException(String message) {
        super(message);
    }

    public AccountAlreadyExistsException(String message, Throwable e) {
        super(message, e);
    }
}
