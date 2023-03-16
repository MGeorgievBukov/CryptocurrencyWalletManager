package crypto.wallet.manager.wallet;

import crypto.wallet.manager.api.ApiCall;
import crypto.wallet.manager.api.ApiCallExecutor;
import crypto.wallet.manager.database.CryptoCoinsInformation;
import crypto.wallet.manager.dto.CryptoCoin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApiCallExecutorTest {

    public static ApiCall apiCall = mock(ApiCall.class);

    @Mock
    public HttpResponse<String> response;

    public static CryptoCoinsInformation cryptoCoinsInformation;

    public static ApiCallExecutor apiCallExecutor;

    @BeforeAll
    public static void setup() {
        cryptoCoinsInformation = new CryptoCoinsInformation();
        apiCallExecutor = new ApiCallExecutor(apiCall, cryptoCoinsInformation);
    }

    @Test
    public void testRunUpdatedInformation() {
        when(apiCall.sendRequest()).thenReturn(response);
        when(response.statusCode()).thenReturn(HTTP_OK);
        when(response.body()).thenReturn("[\n" +
                "  {\n" +
                "    \"asset_id\": \"BTC\",\n" +
                "    \"name\": \"Bitcoin\",\n" +
                "    \"type_is_crypto\": 1,\n" +
                "    \"price_usd\": 10.0\n" +
                "  },\n" +
                "  {\n" +
                "    \"asset_id\": \"ADA\",\n" +
                "    \"name\": \"Cardano\",\n" +
                "    \"type_is_crypto\": 1,\n" +
                "    \"price_usd\": 1.0\n" +
                "  }\n" +
                "]");

        apiCallExecutor.run();

        Set<CryptoCoin> expectedCryptoCoins = new HashSet<>();
        expectedCryptoCoins.add(new CryptoCoin("BTC", "Bitcoin", 10.0, 1));
        expectedCryptoCoins.add(new CryptoCoin("ADA", "Cardano", 1.0, 1));

        assertEquals(expectedCryptoCoins, cryptoCoinsInformation.getCryptoCoinSet(), "Run method is not working properly.");
    }
}
