import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenInfoQuery;
import com.hedera.hashgraph.sdk.TokenUpdateTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class TokenUpdateIntegrationTest {
    @Test
    @DisplayName("Can update token")
    void canUpdateToken() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var response = new TokenCreateTransaction()
            .setTokenName("ffff")
            .setTokenSymbol("F")
            .setDecimals(3)
            .setInitialSupply(1000000)
            .setTreasuryAccountId(testEnv.operatorId)
            .setAdminKey(testEnv.operatorKey)
            .setFreezeKey(testEnv.operatorKey)
            .setWipeKey(testEnv.operatorKey)
            .setKycKey(testEnv.operatorKey)
            .setSupplyKey(testEnv.operatorKey)
            .setFreezeDefault(false)
            .execute(testEnv.client);

        var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

        @Var var info = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(testEnv.client);

        assertThat(info.tokenId).isEqualTo(tokenId);
        assertThat(info.name).isEqualTo("ffff");
        assertThat(info.symbol).isEqualTo("F");
        assertThat(info.decimals).isEqualTo(3);
        assertThat(info.treasuryAccountId).isEqualTo(testEnv.operatorId);
        assertThat(info.adminKey).isNotNull();
        assertThat(info.freezeKey).isNotNull();
        assertThat(info.wipeKey).isNotNull();
        assertThat(info.kycKey).isNotNull();
        assertThat(info.supplyKey).isNotNull();
        assertThat(info.adminKey.toString()).isEqualTo(testEnv.operatorKey.toString());
        assertThat(info.freezeKey.toString()).isEqualTo(testEnv.operatorKey.toString());
        assertThat(info.wipeKey.toString()).isEqualTo(testEnv.operatorKey.toString());
        assertThat(info.kycKey.toString()).isEqualTo(testEnv.operatorKey.toString());
        assertThat(info.supplyKey.toString()).isEqualTo(testEnv.operatorKey.toString());
        assertThat(info.defaultFreezeStatus).isNotNull().isFalse();
        assertThat(info.defaultKycStatus).isNotNull().isFalse();

        new TokenUpdateTransaction()
            .setTokenId(tokenId)
            .setTokenName("aaaa")
            .setTokenSymbol("A")
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        info = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(testEnv.client);

        assertThat(info.tokenId).isEqualTo(tokenId);
        assertThat(info.name).isEqualTo("aaaa");
        assertThat(info.symbol).isEqualTo("A");
        assertThat(info.decimals).isEqualTo(3);
        assertThat(info.treasuryAccountId).isEqualTo(testEnv.operatorId);
        assertThat(info.adminKey).isNotNull();
        assertThat(info.freezeKey).isNotNull();
        assertThat(info.wipeKey).isNotNull();
        assertThat(info.kycKey).isNotNull();
        assertThat(info.supplyKey).isNotNull();
        assertThat(info.adminKey.toString()).isEqualTo(testEnv.operatorKey.toString());
        assertThat(info.freezeKey.toString()).isEqualTo(testEnv.operatorKey.toString());
        assertThat(info.wipeKey.toString()).isEqualTo(testEnv.operatorKey.toString());
        assertThat(info.kycKey.toString()).isEqualTo(testEnv.operatorKey.toString());
        assertThat(info.supplyKey.toString()).isEqualTo(testEnv.operatorKey.toString());
        assertThat(info.defaultFreezeStatus).isNotNull();
        assertThat(info.defaultFreezeStatus).isFalse();
        assertThat(info.defaultKycStatus).isNotNull();
        assertThat(info.defaultKycStatus).isFalse();

        testEnv.close(tokenId);
    }

    @Test
    @DisplayName("Cannot update immutable token")
    void cannotUpdateImmutableToken() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount(new Hbar(10));

        var response = new TokenCreateTransaction()
            .setTokenName("ffff")
            .setTokenSymbol("F")
            .setTreasuryAccountId(testEnv.operatorId)
            .setFreezeDefault(false)
            .execute(testEnv.client);

        var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

        assertThatExceptionOfType(ReceiptStatusException.class).isThrownBy(() -> {
            new TokenUpdateTransaction()
                .setTokenId(tokenId)
                .setTokenName("aaaa")
                .setTokenSymbol("A")
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        }).withMessageContaining(Status.TOKEN_IS_IMMUTABLE.toString());

        // we lose this IntegrationTestEnv throwaway account
        testEnv.client.close();
    }
}
