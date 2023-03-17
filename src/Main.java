import crypto.wallet.manager.api.ApiCall;
import crypto.wallet.manager.command.CommandExecutor;
import crypto.wallet.manager.database.Accounts;
import crypto.wallet.manager.database.CryptoCoinsInformation;
import crypto.wallet.manager.database.Logs;
import crypto.wallet.manager.server.CryptocurrencyWalletManagerServer;

import java.io.File;
import java.net.http.HttpClient;

public class Main {
    private static final String API_KEY = "EA2AC686-EED9-4F0F-B3CF-53D9AA9F9054";

    private static final String ACCOUNTS_PATH = "database" + File.separator + "accounts.dat";

    private static final String WALLET_PATH = "database" + File.separator + "wallets.dat";

    public static void main(String[] args) {
        try (Accounts accounts = new Accounts(ACCOUNTS_PATH, WALLET_PATH)) {
            accounts.loadData(accounts.getAccountsPath(), accounts.getWalletsPath());
            ApiCall apiCall = new ApiCall(HttpClient.newBuilder().build(), API_KEY);
            CommandExecutor commandExecutor = new CommandExecutor(accounts, new CryptoCoinsInformation());
            CryptocurrencyWalletManagerServer cryptocurrencyWalletManagerServer
                    = new CryptocurrencyWalletManagerServer(commandExecutor);
            cryptocurrencyWalletManagerServer.start(apiCall);
        } catch (Exception e) {
            Logs.logErrorWithStackTrace(e.getStackTrace(),
                    "Problem occurred while writing wallets and accounts data to file.", Logs.DEFAULT_LOG_PATH);
        }
    }
}
