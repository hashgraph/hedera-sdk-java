import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.*;

import java.util.*;
import javax.annotation.Nullable;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertNotNull;


public class IntegrationTestEnv {
    public Client client;
    public PrivateKey operatorKey;
    public AccountId operatorId;
    public List<AccountId> nodeAccountIds;
    public List<AccountId> nodeAccountIdsForChunked;
    public static Random random = new Random();

    private AccountId originalOperatorId;

    public IntegrationTestEnv() throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        if (System.getProperty("HEDERA_NETWORK").equals("previewnet")) {
            client = Client.forPreviewnet();
        } else if (System.getProperty("HEDERA_NETWORK").equals("localhost")) {
            var network = new Hashtable<String, AccountId>();
            network.put("127.0.0.1:50213", new AccountId(3));
            network.put("127.0.0.1:50214", new AccountId(4));
            network.put("127.0.0.1:50215", new AccountId(5));

            client = Client.forNetwork(network);
        } else {
            try {
                client = Client.fromConfigFile(System.getProperty("CONFIG_FILE"));
            } catch (Exception e) {
                client = Client.forTestnet();
            }
        }

        try {
            operatorKey = PrivateKey.fromString(System.getProperty("OPERATOR_KEY"));
            operatorId = AccountId.fromString(System.getProperty("OPERATOR_ID"));

            client.setOperator(operatorId, operatorKey);
        } catch (Exception e) {
        }

        assertNotNull(client.getOperatorAccountId());
        assertNotNull(client.getOperatorPublicKey());

        var key = PrivateKey.generate();

        var response = new AccountCreateTransaction()
            .setInitialBalance(new Hbar(130))
            .setKey(key)
            .execute(client);

        operatorId = Objects.requireNonNull(response.getReceipt(client).accountId);
        operatorKey = key;
        nodeAccountIds = Collections.singletonList(response.nodeId);
        // chunked transactions can have tricky bugs when there are multiple nodes
        // and multiple chunks.  Need to add a second nodeId to catch these bugs.
        nodeAccountIdsForChunked = new ArrayList<>();
        nodeAccountIdsForChunked.add(response.nodeId);
        for(var nodeId : client.getNetwork().values()) {
            if( ! nodeAccountIdsForChunked.contains(nodeId)) {
                nodeAccountIdsForChunked.add(nodeId);
                break;
            }
        }
        originalOperatorId = client.getOperatorAccountId();
        client.setOperator(operatorId, operatorKey);
    }

    public void cleanUpAndClose(
        @Nullable TokenId newTokenId,
        @Nullable AccountId newAccountId,
        @Nullable PrivateKey newAccountKey
    ) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        if (newTokenId != null) {
            new TokenDeleteTransaction()
                .setNodeAccountIds(nodeAccountIds)
                .setTokenId(newTokenId)
                .execute(client)
                .getReceipt(client);
        }

        if(newAccountId != null) {
            new AccountDeleteTransaction()
                .setNodeAccountIds(nodeAccountIds)
                .setTransferAccountId(originalOperatorId)
                .setAccountId(newAccountId)
                .freezeWith(client)
                .sign(newAccountKey)
                .execute(client)
                .getReceipt(client);
        }

        new AccountDeleteTransaction()
            .setNodeAccountIds(nodeAccountIds)
            .setTransferAccountId(originalOperatorId)
            .setAccountId(operatorId)
            .execute(client)
            .getReceipt(client);

        client.close();
    }

    public void cleanUpAndClose() throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        cleanUpAndClose(null, null, null);
    }

    public void cleanUpAndClose(
        AccountId newAccountId,
        PrivateKey newAccountKey
    ) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        cleanUpAndClose(null, newAccountId, newAccountKey);
    }

    public void cleanUpAndClose(
        TokenId newTokenId
    ) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        cleanUpAndClose(newTokenId, null, null);
    }
}
