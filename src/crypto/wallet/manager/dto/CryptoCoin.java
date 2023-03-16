package crypto.wallet.manager.dto;

import com.google.gson.annotations.SerializedName;

public class CryptoCoin {
    private static final int IS_CRYPTO = 1;

    @SerializedName("asset_id")
    private final String offeringCode;

    private final String name;

    @SerializedName("price_usd")
    private final double priceUSD;

    @SerializedName("type_is_crypto")
    private final int isCrypto;


    public CryptoCoin(String offeringCode) {
        this.offeringCode = offeringCode;
        this.name = null;
        this.priceUSD = 0.0;
        isCrypto = IS_CRYPTO;
    }

    public CryptoCoin(String offeringCode, String name, double priceUSD, int isCrypto) {
        this.offeringCode = offeringCode;
        this.name = name;
        this.priceUSD = priceUSD;
        this.isCrypto = isCrypto;
    }

    public boolean isCrypto() {
        return isCrypto == IS_CRYPTO;
    }

    public String getOfferingCode() {
        return offeringCode;
    }

    public String getName() {
        return name;
    }

    public double getPriceUSD() {
        return priceUSD;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CryptoCoin that = (CryptoCoin) o;

        return offeringCode.equals(that.offeringCode);
    }

    @Override
    public int hashCode() {
        return offeringCode.hashCode();
    }
}
