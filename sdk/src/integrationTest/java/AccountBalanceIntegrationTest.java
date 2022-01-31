import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.MaxAttemptsExceededException;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountBalanceIntegrationTest {
    @Test
    @DisplayName("can connect to previewnwet with TLS")
    void canConnectToPreviewnetWithTLS() throws Exception {
        var client = Client.forPreviewnet()
            .setTransportSecurity(true);

        for (var entry : client.getNetwork().entrySet()) {
            assertTrue(entry.getKey().endsWith(":50212"));

            new AccountBalanceQuery()
                .setNodeAccountIds(Collections.singletonList(entry.getValue()))
                .setAccountId(entry.getValue())
                .execute(client);
        }
        client.close();
    }

    @Test
    @DisplayName("can connect to testnet with TLS")
    void canConnectToTestnetWithTLS() throws Exception {
        var client = Client.forTestnet()
            .setTransportSecurity(true);

        for (var entry : client.getNetwork().entrySet()) {
            assertTrue(entry.getKey().endsWith(":50212"));

            new AccountBalanceQuery()
                .setNodeAccountIds(Collections.singletonList(entry.getValue()))
                .setAccountId(entry.getValue())
                .execute(client);
        }

        client.close();
    }

    /*
     * Disabled because not all nodes/proxies support TLS.
     * List as of Dec/13/2021:
    succeeded for 3.18.18.254:50212=0.0.5
    succeeded for 34.239.82.6:50212=0.0.3
    succeeded for 50.7.124.46:50212=0.0.16
    succeeded for 52.78.202.34:50212=0.0.22
    succeeded for 107.155.64.98:50212=0.0.5
    failed for 18.135.7.211:50212=0.0.24
    succeeded for 35.186.191.247:50212=0.0.4
    succeeded for 3.121.238.26:50212=0.0.15
    failed for 35.236.5.219:50212=0.0.8
    succeeded for 15.164.44.66:50212=0.0.3
    succeeded for 35.232.244.145:50212=0.0.23
    succeeded for 15.165.118.251:50212=0.0.3
    succeeded for 18.232.251.19:50212=0.0.17
    succeeded for 35.237.200.180:50212=0.0.3
    succeeded for 65.52.68.254:50212=0.0.10
    succeeded for 172.105.247.67:50212=0.0.18
    succeeded for 13.53.119.185:50212=0.0.11
    failed for 35.183.66.150:50212=0.0.8
    succeeded for 34.64.141.166:50212=0.0.22
    succeeded for 35.204.86.32:50212=0.0.12
    succeeded for 139.162.156.222:50212=0.0.18
    succeeded for 23.97.247.27:50212=0.0.11
    succeeded for 3.18.91.176:50212=0.0.23
    succeeded for 3.248.27.48:50212=0.0.10
    succeeded for 35.236.2.27:50212=0.0.14
    succeeded for 35.192.2.25:50212=0.0.5
    failed for 23.97.237.125:50212=0.0.9
    succeeded for 13.71.90.154:50212=0.0.6
    succeeded for 23.111.186.250:50212=0.0.5
    failed for 104.211.205.124:50212=0.0.6
    succeeded for 69.87.221.231:50212=0.0.11
    succeeded for 34.87.150.174:50212=0.0.26
    succeeded for 35.199.161.108:50212=0.0.6
    succeeded for 35.240.118.96:50212=0.0.11
    succeeded for 69.87.222.61:50212=0.0.11
    succeeded for 34.215.192.104:50212=0.0.13
    succeeded for 34.93.112.7:50212=0.0.25
    failed for 198.16.99.40:50212=0.0.16
    failed for 35.181.158.250:50212=0.0.9
    failed for 35.197.192.225:50212=0.0.9
    succeeded for 35.242.233.154:50212=0.0.10
    succeeded for 40.89.139.247:50212=0.0.15
    succeeded for 13.228.103.14:50212=0.0.26
    succeeded for 13.69.120.73:50212=0.0.16
    succeeded for 35.203.82.240:50212=0.0.7
    succeeded for 35.228.11.53:50212=0.0.15
    succeeded for 13.235.15.32:50212=0.0.6
    succeeded for 50.7.176.235:50212=0.0.16
    succeeded for 18.157.223.230:50212=0.0.16
    succeeded for 40.114.107.85:50212=0.0.14
    failed for 13.64.151.232:50212=0.0.6
    failed for 18.168.4.59:50212=0.0.19
    succeeded for 13.77.151.212:50212=0.0.20
    failed for 34.89.103.38:50212=0.0.24
    succeeded for 104.43.194.202:50212=0.0.5
    succeeded for 34.86.212.247:50212=0.0.17
    succeeded for 137.116.36.18:50212=0.0.4
    succeeded for 34.91.181.183:50212=0.0.16
    failed for 31.214.8.131:50212=0.0.9
    succeeded for 172.104.150.132:50212=0.0.18
    succeeded for 13.77.158.252:50212=0.0.13
    succeeded for 3.130.52.236:50212=0.0.4
    succeeded for 13.36.123.209:50212=0.0.21
    succeeded for 23.102.74.34:50212=0.0.7
    succeeded for 35.177.162.180:50212=0.0.12
    succeeded for 179.190.33.184:50212=0.0.10
    succeeded for 96.126.72.172:50212=0.0.11
    succeeded for 35.234.132.107:50212=0.0.13
    succeeded for 34.76.140.109:50212=0.0.21
    succeeded for 13.82.40.153:50212=0.0.3
    succeeded for 34.82.78.255:50212=0.0.20
    succeeded for 74.50.117.35:50212=0.0.5
    succeeded for 51.140.102.228:50212=0.0.12
    succeeded for 13.232.240.207:50212=0.0.25
    failed for 34.89.87.138:50212=0.0.19
    succeeded for 52.8.21.141:50212=0.0.14
    failed for 51.140.43.81:50212=0.0.19
    succeeded for 13.124.142.126:50212=0.0.3
    succeeded for 3.114.54.4:50212=0.0.7
    failed for 23.96.185.18:50212=0.0.8
    succeeded for 40.114.92.39:50212=0.0.17
    succeeded for 13.52.108.243:50212=0.0.6
     */

    @Test
    @Disabled
    @DisplayName("can connect to mainnet with TLS")
    void canConnectToMainnetWithTLS() throws Exception {
        var client = Client.forMainnet()
            .setTransportSecurity(true);

        for (var entry : client.getNetwork().entrySet()) {
            assertTrue(entry.getKey().endsWith(":50212"));

            try {
                new AccountBalanceQuery()
                    .setMaxAttempts(1)
                    .setNodeAccountIds(Collections.singletonList(entry.getValue()))
                    .setAccountId(entry.getValue())
                    .execute(client);
                System.out.println("succeeded for " + entry);
            } catch (Throwable error) {
                System.out.println("failed for " + entry);
            }
        }

        client.close();
    }

    @Test
    @DisplayName("can connect to previewnet with certificate verification off")
    void cannotConnectToPreviewnetWhenNetworkNameIsNullAndCertificateVerificationIsEnabled() throws Exception {
        var client = Client.forPreviewnet()
            .setTransportSecurity(true)
            .setVerifyCertificates(true)
            .setNetworkName(null);

        assertFalse(client.getNetwork().isEmpty());

        for (var entry : client.getNetwork().entrySet()) {
            assertTrue(entry.getKey().endsWith(":50212"));

            assertThrows(IllegalStateException.class, () -> {
                new AccountBalanceQuery()
                    .setNodeAccountIds(Collections.singletonList(entry.getValue()))
                    .setAccountId(entry.getValue())
                    .execute(client);
            });
        }

        client.close();
    }

    @Test
    @DisplayName("Can fetch balance for client operator")
    void canFetchBalanceForClientOperator() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var balance = new AccountBalanceQuery()
            .setAccountId(testEnv.operatorId)
            .execute(testEnv.client);

        assertTrue(balance.hbars.toTinybars() > 0);

        testEnv.close();
    }

    @Test
    @DisplayName("Can fetch cost for the query")
    void getCostBalanceForClientOperator() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var balance = new AccountBalanceQuery()
            .setAccountId(testEnv.operatorId)
            .setMaxQueryPayment(new Hbar(1));

        var cost = balance.getCost(testEnv.client);

        var accBalance = balance.setQueryPayment(cost).execute(testEnv.client);

        assertTrue(accBalance.hbars.toTinybars() > 0);
        assertEquals(0, cost.toTinybars());

        testEnv.close();
    }

    @Test
    @DisplayName("Can fetch cost for the query, big max set")
    void getCostBigMaxBalanceForClientOperator() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var balance = new AccountBalanceQuery()
            .setAccountId(testEnv.operatorId)
            .setMaxQueryPayment(new Hbar(1000000));

        var cost = balance.getCost(testEnv.client);

        var accBalance = balance.setQueryPayment(cost).execute(testEnv.client);

        assertTrue(accBalance.hbars.toTinybars() > 0);

        testEnv.close();
    }

    @Test
    @DisplayName("Can fetch cost for the query, very small max set")
    void getCostSmallMaxBalanceForClientOperator() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var balance = new AccountBalanceQuery()
            .setAccountId(testEnv.operatorId)
            .setMaxQueryPayment(Hbar.fromTinybars(1));

        var cost = balance.getCost(testEnv.client);

        var accBalance = balance.setQueryPayment(cost).execute(testEnv.client);

        assertTrue(accBalance.hbars.toTinybars() > 0);

        testEnv.close();
    }

    @Test
    @DisplayName("Cannot fetch balance for invalid account ID")
    void canNotFetchBalanceForInvalidAccountId() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var error = assertThrows(PrecheckStatusException.class, () -> {
            new AccountBalanceQuery()
                .setAccountId(AccountId.fromString("1.0.3"))
                .execute(testEnv.client);
        });

        assertTrue(error.getMessage().contains(Status.INVALID_ACCOUNT_ID.toString()));

        testEnv.close();
    }

    @Test
    @DisplayName("Can fetch token balances for client operator")
    void canFetchTokenBalancesForClientOperator() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var response = new TokenCreateTransaction()
            .setTokenName("ffff")
            .setTokenSymbol("F")
            .setInitialSupply(10000)
            .setDecimals(50)
            .setTreasuryAccountId(testEnv.operatorId)
            .setAdminKey(testEnv.operatorKey)
            .setSupplyKey(testEnv.operatorKey)
            .setFreezeDefault(false)
            .execute(testEnv.client);

        var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

        var balance = new AccountBalanceQuery()
            .setAccountId(testEnv.operatorId)
            .execute(testEnv.client);

        assertEquals(10000, balance.tokens.get(tokenId));
        assertEquals(50, balance.tokenDecimals.get(tokenId));

        testEnv.close(tokenId);
    }
}
