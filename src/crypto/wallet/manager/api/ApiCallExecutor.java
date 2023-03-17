package crypto.wallet.manager.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import crypto.wallet.manager.database.CryptoCoinsInformation;
import crypto.wallet.manager.database.Logs;
import crypto.wallet.manager.dto.CryptoCoin;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.Set;

public class ApiCallExecutor implements Runnable {

    private static final String RESPONSE_NULL_MESSAGE = "Couldn't get a response from the API service";

    private final Gson gson;

    private final ApiCall apiCall;

    private final CryptoCoinsInformation cryptoCoinsInformation;

    public ApiCallExecutor(ApiCall apiCall, CryptoCoinsInformation cryptoCoinsInformation) {
        this.apiCall = apiCall;
        this.cryptoCoinsInformation = cryptoCoinsInformation;
        gson = new Gson();
    }

    @Override
    public void run() {
        HttpResponse<String> response = apiCall.sendRequest();
        if (response != null) {
            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                Type type = new TypeToken<Set<CryptoCoin>>() {
                }.getType();
                Set<CryptoCoin> data = gson.fromJson(response.body(), type);
                cryptoCoinsInformation.updateCryptoCoinSetInformation(data);
            } else {
                Logs.logErrorMessage(response.body(), Logs.DEFAULT_LOG_PATH);
            }
        } else {
            Logs.logErrorMessage(RESPONSE_NULL_MESSAGE, Logs.DEFAULT_LOG_PATH);
        }
    }
}
