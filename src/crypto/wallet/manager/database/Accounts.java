package crypto.wallet.manager.database;

import crypto.wallet.manager.account.Account;
import crypto.wallet.manager.exceptions.AccountAlreadyExistsException;
import crypto.wallet.manager.exceptions.AccountDoesNotExistException;
import crypto.wallet.manager.exceptions.AccountIsAlreadyLoggedInException;
import crypto.wallet.manager.wallet.BasicWallet;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Accounts implements AutoCloseable {

    private final String accountsPath;

    private final String walletsPath;

    private static Map<Account, BasicWallet> accountsAndWallets;

    private static Set<Account> currentlyUsedAccounts;

    public Map<Account, BasicWallet> getAccountsAndWallets() {
        return Collections.unmodifiableMap(accountsAndWallets);
    }

    public Accounts(String accountsPath, String walletsPath) {
        accountsAndWallets = new LinkedHashMap<>();
        currentlyUsedAccounts = new HashSet<>();
        this.accountsPath = accountsPath;
        this.walletsPath = walletsPath;
    }

    public void logOut(Account account) {
        currentlyUsedAccounts.remove(account);
    }

    public String getAccountsPath() {
        return accountsPath;
    }

    public String getWalletsPath() {
        return walletsPath;
    }

    public void loadData(String accountsPath, String walletsPath) {
        try {
            Files.createDirectories(Paths.get("database"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Set<Account> accountsSet = new LinkedHashSet<>();
        List<BasicWallet> walletsList = new ArrayList<>();

        readAccountsFromFile(accountsSet, accountsPath);
        readWalletsFromFile(walletsList, walletsPath);

        BasicWallet[] basicWalletArray = new BasicWallet[walletsList.size()];
        Account[] accountArray = new Account[accountsSet.size()];

        mergeAccountsAndWallets(walletsList.toArray(basicWalletArray), accountsSet.toArray(accountArray));
    }

    public BasicWallet getWalletOf(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null.");
        }

        return accountsAndWallets.get(account);
    }

    public void createAccount(Account account) throws AccountAlreadyExistsException {
        if (accountsAndWallets.containsKey(account)) {
            throw new AccountAlreadyExistsException("Cannot register an account with" +
                    " the same name as another from the database");
        }

        accountsAndWallets.put(account, new BasicWallet());
    }

    public Account login(String username, String password)
            throws AccountDoesNotExistException, AccountIsAlreadyLoggedInException {
        for (var account : accountsAndWallets.keySet()) {
            if (account.passwordMatch(password) && account.equals(new Account(username))) {
                if (currentlyUsedAccounts.contains(account)) {
                    throw new AccountIsAlreadyLoggedInException("Account is already in use.");
                }

                currentlyUsedAccounts.add(account);
                return account;
            }
        }

        throw new AccountDoesNotExistException("No account with these credentials exists in the database");
    }

    private static void mergeAccountsAndWallets(BasicWallet[] basicWalletSet, Account[] accountSet) {
        int count = basicWalletSet.length;
        for (int i = 0; i < count; i++) {
            accountsAndWallets.put(accountSet[i], basicWalletSet[i]);
        }
    }

    private void writeAccountsToFile(String accountsPath) {
        Path pathOfAccounts = Path.of(accountsPath);

        try (var objectOutputStream = new ObjectOutputStream(Files.newOutputStream(pathOfAccounts))) {
            for (Account account : accountsAndWallets.keySet()) {
                objectOutputStream.writeObject(account);
                objectOutputStream.flush();
            }
        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while writing to a file", e);
        }
    }

    private void writeWalletsToFile(String walletsPath) {
        Path pathOfWallets = Path.of(walletsPath);

        try (var objectOutputStream = new ObjectOutputStream(Files.newOutputStream(pathOfWallets))) {
            for (BasicWallet basicWallet : accountsAndWallets.values()) {
                objectOutputStream.writeObject(basicWallet);
                objectOutputStream.flush();
            }
        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while writing to a file", e);
        }
    }

    private void readAccountsFromFile(Set<Account> accountSet, String accountsPath) {
        Path pathOfAccounts = Path.of(accountsPath);

        if (Files.exists(pathOfAccounts))
            try (var objectInputStream = new ObjectInputStream(Files.newInputStream(pathOfAccounts))) {

                Object accountObject;
                while ((accountObject = objectInputStream.readObject()) != null) {
                    Account s = (Account) accountObject;
                    accountSet.add(s);
                }

            } catch (EOFException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (FileNotFoundException e) {
                throw new IllegalStateException("The files does not exist", e);
            } catch (IOException e) {
                throw new IllegalStateException("A problem occurred while reading from a file", e);
            }
        else {
            try {
                Files.createFile(pathOfAccounts);
            } catch (IOException e) {
                Logs.logErrorWithStackTrace(e.getStackTrace(),
                        "Could not create file to store account information in.", Logs.LOG_PATH);
                throw new RuntimeException(e);
            }
        }
    }

    private void readWalletsFromFile(List<BasicWallet> basicWalletList, String walletsPath) {
        Path pathOfWallets = Path.of(walletsPath);

        if (Files.exists(pathOfWallets))
            try (var objectInputStream = new ObjectInputStream(Files.newInputStream(pathOfWallets))) {

                Object walletObject;
                while ((walletObject = objectInputStream.readObject()) != null) {
                    BasicWallet s = (BasicWallet) walletObject;
                    basicWalletList.add(s);
                }

            } catch (EOFException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (FileNotFoundException e) {
                throw new IllegalStateException("The files does not exist", e);
            } catch (IOException e) {
                throw new IllegalStateException("A problem occurred while reading from a file", e);
            }
        else {
            try {
                Files.createFile(pathOfWallets);
            } catch (IOException e) {
                Logs.logErrorWithStackTrace(e.getStackTrace(),
                        "Could not create file to store wallet information in.", Logs.LOG_PATH);
                throw new RuntimeException(e);
            }
        }


    }

    @Override
    public void close() throws Exception {
        writeWalletsToFile(walletsPath);
        writeAccountsToFile(accountsPath);
    }
}
