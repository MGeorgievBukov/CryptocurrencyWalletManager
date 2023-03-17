package crypto.wallet.manager.command;

import crypto.wallet.manager.account.Account;
import crypto.wallet.manager.database.Accounts;
import crypto.wallet.manager.database.CryptoCoinsInformation;
import crypto.wallet.manager.database.Logs;
import crypto.wallet.manager.exceptions.AccountAlreadyExistsException;
import crypto.wallet.manager.exceptions.AccountDoesNotExistException;
import crypto.wallet.manager.exceptions.AccountIsAlreadyLoggedInException;
import crypto.wallet.manager.exceptions.CryptoCoinDoesNotExistException;
import crypto.wallet.manager.exceptions.InsufficientBalanceException;

import java.nio.channels.SelectionKey;

public class CommandExecutor {
    private static final String LOGIN = "login";

    private static final String REGISTER = "register";

    private static final String DEPOSIT = "deposit";

    private static final String LIST_CRYPTOS = "list-cryptos";

    private static final String BUY_CRYPTO = "buy-crypto";

    private static final String SELL_CRYPTO = "sell-crypto";

    private static final String GET_WALLET_INFORMATION = "get-wallet-information";

    private static final String GET_WALLET_INVESTMENT_INFORMATION = "get-wallet-investment-information";

    private static final String INVALID_INPUT_ARGUMENTS = "This is not the correct way to use this command";

    private static final String SUCCESSFUL_OPERATION_MESSAGE = "Transaction completed";

    private static final String MUST_LOGIN_MESSAGE = "Log in first or create a new account if you don't have one.";

    private static final String ALREADY_LOGGED_IN_MESSAGE = "You are already logged in.";

    private static final String ALREADY_LOGGED_IN_REGISTER_MESSAGE
            = "Cannot make a new account while already logged in.";

    private static final String NEGATIVE_AMOUNT_MESSAGE = "Amount cannot be negative.";

    private static final String INVALID_FORMAT_AMOUNT_MESSAGE = "Invalid format for an amount of money.";

    private static final String INSUFFICIENT_AMOUNT_MESSAGE = "There is not enough available balance in your wallet.";

    private static final String CRYPTO_COIN_DOES_NOT_EXIST_MESSAGE = "No crypto coin exists with this id.";

    private static final String SERVER_SIDE_ERROR_MESSAGE = "An error occurred on the server. Try again later.";

    private static final String INVALID_LOGIN_MESSAGE = "Wrong username or password.";

    private static final String ACCOUNT_ALREADY_LOGGED_IN_MESSAGE = "Account is already logged in.";

    private static final String CRYPTO_COIN_STORAGE_PROBLEM_LOG_MESSAGE
            = "There is a problem with the crypto coin storage of user ";

    private static final String REGISTER_SUCCESSFUL_MESSAGE = "Register successful";

    private static final String LOGIN_SUCCESSFUL_MESSAGE = "Login successful";

    private static final String ACCOUNT_EXISTS_MESSAGE = "Account already exists";

    private static final String PROBLEM_WHILE_LOGGING_IN_MESSAGE
            = "A problem occurred while trying to log in. Try again.";

    private static final String LOGIN_PROBLEM_LOG_MESSAGE
            = "A problem occurred while trying to match passwords for user ";

    private static final int NUMBER_OF_ARGUMENTS_BUY_CRYPTO_AND_LOGIN_AND_REGISTER = 2;

    private static final int NUMBER_OF_ARGUMENTS_DEPOSIT_AND_SELL_CRYPTO = 1;

    private static final String DISCONNECT = "disconnect";

    private static final String DISCONNECTED = "disconnected";

    private static final String UNKNOWN_COMMAND_MESSAGE = "Unknown command";

    private static final String HELP = "help";

    private static final String SHUTDOWN = "shutdown";

    private final Accounts accounts;

    private final CryptoCoinsInformation cryptoCoinsInformation;

    public CommandExecutor(Accounts accounts, CryptoCoinsInformation cryptoCoinsInformation) {
        this.accounts = accounts;
        this.cryptoCoinsInformation = cryptoCoinsInformation;
    }

    public CryptoCoinsInformation getCryptoCoinsInformation() {
        return cryptoCoinsInformation;
    }

    public String execute(Command command, SelectionKey key) {
        return switch (command.command()) {
            case LOGIN -> login(command.arguments(), key);
            case REGISTER -> register(command.arguments(), key);
            case DEPOSIT -> deposit(command.arguments(), key);
            case LIST_CRYPTOS -> listCryptos(key);
            case BUY_CRYPTO -> buyCrypto(command.arguments(), key);
            case SELL_CRYPTO -> sellCrypto(command.arguments(), key);
            case GET_WALLET_INFORMATION -> getWalletInformation(key);
            case GET_WALLET_INVESTMENT_INFORMATION -> getWalletInvestmentInformation(key);
            case DISCONNECT -> disconnect(key);
            case HELP -> help();
            case SHUTDOWN -> SHUTDOWN;
            default -> UNKNOWN_COMMAND_MESSAGE;
        };
    }

    private String help() {
        return "Available commands: " + System.lineSeparator() +
                "login {name} {password}" + System.lineSeparator() +
                "register {name} {password}" + System.lineSeparator() +
                "deposit {amount}" + System.lineSeparator() +
                "list-cryptos" + System.lineSeparator() +
                "buy-crypto {id} {amount}" + System.lineSeparator() +
                "sell-crypto {id}" + System.lineSeparator() +
                "wallet-information" + System.lineSeparator() +
                "wallet-investment-information" + System.lineSeparator() +
                "disconnect" + System.lineSeparator();
    }

    private String disconnect(SelectionKey key) {
        Account account;

        if (key.attachment() == null) {
            return MUST_LOGIN_MESSAGE;
        } else {
            account = (Account) key.attachment();
        }

        accounts.logOut(account);
        return DISCONNECTED;
    }

    private String login(String[] args, SelectionKey key) {
        if (key.attachment() != null) {
            return ALREADY_LOGGED_IN_MESSAGE;
        }

        if (args.length != NUMBER_OF_ARGUMENTS_BUY_CRYPTO_AND_LOGIN_AND_REGISTER) {
            return INVALID_INPUT_ARGUMENTS;
        }

        String username = args[0];
        String password = args[1];

        try {
            Account account = accounts.login(username, password);
            key.attach(account);
        } catch (AccountDoesNotExistException e) {
            return INVALID_LOGIN_MESSAGE;
        } catch (AccountIsAlreadyLoggedInException e) {
            return ACCOUNT_ALREADY_LOGGED_IN_MESSAGE;
        } catch (IllegalArgumentException e) {
            Logs.logErrorWithStackTrace(e.getStackTrace(), LOGIN_PROBLEM_LOG_MESSAGE + username, Logs.DEFAULT_LOG_PATH);
            return PROBLEM_WHILE_LOGGING_IN_MESSAGE;
        }

        return LOGIN_SUCCESSFUL_MESSAGE;
    }

    private String register(String[] args, SelectionKey key) {
        if (key.attachment() != null) {
            return ALREADY_LOGGED_IN_REGISTER_MESSAGE;
        }

        if (args.length != NUMBER_OF_ARGUMENTS_BUY_CRYPTO_AND_LOGIN_AND_REGISTER) {
            return INVALID_INPUT_ARGUMENTS;
        }

        String username = args[0];
        String password = args[1];

        try {
            Account account = new Account(username, password);
            accounts.createAccount(account);
        } catch (AccountAlreadyExistsException e) {
            return ACCOUNT_EXISTS_MESSAGE;
        }

        return REGISTER_SUCCESSFUL_MESSAGE;
    }

    private String deposit(String[] args, SelectionKey key) {
        Account account;

        if (key.attachment() == null) {
            return MUST_LOGIN_MESSAGE;
        } else {
            account = (Account) key.attachment();
        }

        if (args.length != NUMBER_OF_ARGUMENTS_DEPOSIT_AND_SELL_CRYPTO) {
            return INVALID_INPUT_ARGUMENTS;
        }

        try {
            double amount = Double.parseDouble(args[0]);
            accounts.getWalletOf(account).deposit(amount);
        } catch (NumberFormatException | NullPointerException e) {
            return INVALID_FORMAT_AMOUNT_MESSAGE;
        } catch (IllegalArgumentException e) {
            return NEGATIVE_AMOUNT_MESSAGE;
        }

        return SUCCESSFUL_OPERATION_MESSAGE;
    }

    private String listCryptos(SelectionKey key) {
        if (key.attachment() == null) {
            return MUST_LOGIN_MESSAGE;
        }

        return cryptoCoinsInformation.listOfferings();
    }

    private String buyCrypto(String[] args, SelectionKey key) {
        Account account;

        if (key.attachment() == null) {
            return MUST_LOGIN_MESSAGE;
        } else {
            account = (Account) key.attachment();
        }

        if (args.length != NUMBER_OF_ARGUMENTS_BUY_CRYPTO_AND_LOGIN_AND_REGISTER) {
            return INVALID_INPUT_ARGUMENTS;
        }

        try {
            String offeringCode = args[0];
            double amount = Double.parseDouble(args[1]);
            accounts.getWalletOf(account).buyCryptoCoin(amount, offeringCode, cryptoCoinsInformation);
        } catch (NumberFormatException | NullPointerException e) {
            return INVALID_FORMAT_AMOUNT_MESSAGE;
        } catch (IllegalArgumentException e) {
            return NEGATIVE_AMOUNT_MESSAGE;
        } catch (InsufficientBalanceException e) {
            return INSUFFICIENT_AMOUNT_MESSAGE;
        } catch (CryptoCoinDoesNotExistException e) {
            return CRYPTO_COIN_DOES_NOT_EXIST_MESSAGE;
        }

        return SUCCESSFUL_OPERATION_MESSAGE;
    }

    private String sellCrypto(String[] args, SelectionKey key) {
        Account account;

        if (key.attachment() == null) {
            return MUST_LOGIN_MESSAGE;
        } else {
            account = (Account) key.attachment();
        }

        if (args.length != NUMBER_OF_ARGUMENTS_DEPOSIT_AND_SELL_CRYPTO) {
            return INVALID_INPUT_ARGUMENTS;
        }

        String offeringCode = args[0];

        try {
            accounts.getWalletOf(account).sellCryptoCoin(offeringCode, cryptoCoinsInformation);
        } catch (CryptoCoinDoesNotExistException e) {
            return CRYPTO_COIN_DOES_NOT_EXIST_MESSAGE;
        }

        return SUCCESSFUL_OPERATION_MESSAGE;
    }

    private String getWalletInformation(SelectionKey key) {
        Account account;

        if (key.attachment() == null) {
            return MUST_LOGIN_MESSAGE;
        } else {
            account = (Account) key.attachment();
        }

        return accounts.getWalletOf(account).walletInformation();
    }

    private String getWalletInvestmentInformation(SelectionKey key) {
        Account account;

        if (key.attachment() == null) {
            return MUST_LOGIN_MESSAGE;
        } else {
            account = (Account) key.attachment();
        }

        try {
            return accounts.getWalletOf(account).walletInvestmentInformation(cryptoCoinsInformation);
        } catch (CryptoCoinDoesNotExistException e) {
            Logs.logErrorWithStackTrace(e.getStackTrace(), CRYPTO_COIN_STORAGE_PROBLEM_LOG_MESSAGE
                    + account.getUsername() + System.lineSeparator(), Logs.DEFAULT_LOG_PATH);
            return SERVER_SIDE_ERROR_MESSAGE;
        }
    }
}
