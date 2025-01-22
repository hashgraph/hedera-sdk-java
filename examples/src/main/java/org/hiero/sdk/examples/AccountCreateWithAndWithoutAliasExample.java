package org.hiero.sdk.examples;

import io.github.cdimascio.dotenv.Dotenv;
import org.bouncycastle.util.encoders.Hex;
import org.hiero.sdk.*;
import org.hiero.sdk.logger.LogLevel;
import org.hiero.sdk.logger.Logger;

import java.util.Objects;

public class AccountCreateWithAndWithoutAliasExample {
    private static final String DEFAULT_NETWORK = "testnet";
    private static final String DEFAULT_LOG_LEVEL = "SILENT";

    private final Client client;
    private final AccountId operatorId;
    private final PrivateKey operatorKey;

    public AccountCreateWithAndWithoutAliasExample() throws InterruptedException {
        Dotenv env = Dotenv.load();
        this.operatorId = AccountId.fromString(Objects.requireNonNull(env.get("OPERATOR_ID"), "OPERATOR_ID must be set"));
        this.operatorKey = PrivateKey.fromString(Objects.requireNonNull(env.get("OPERATOR_KEY"), "OPERATOR_KEY must be set"));
        String network = env.get("HEDERA_NETWORK", DEFAULT_NETWORK);
        String logLevel = env.get("SDK_LOG_LEVEL", DEFAULT_LOG_LEVEL);

        this.client = initializeClient(network, logLevel);
    }

    private Client initializeClient(String network, String logLevel) throws InterruptedException {
        Client client = ClientHelper.forName(network);
        client.setOperator(operatorId, operatorKey);
        client.setLogger(new Logger(LogLevel.valueOf(logLevel)));
        return client;
    }

    public void createAccountWithAlias() throws Exception {
        PrivateKey privateKey = PrivateKey.generateECDSA();
        PublicKey publicKey = privateKey.getPublicKey();
        EvmAddress evmAddress = publicKey.toEvmAddress();

        AccountCreateTransaction transaction = new AccountCreateTransaction()
            .setKeyWithAlias(privateKey)
            .freezeWith(client);

        transaction.sign(privateKey);
        TransactionResponse response = transaction.execute(client);

        AccountId accountId = response.getReceipt(client).accountId;
        AccountInfo info = new AccountInfoQuery()
            .setAccountId(accountId)
            .execute(client);

        System.out.println("Initial EVM address: " + evmAddress + " is the same as " + info.contractAccountId);
    }

    public void createAccountWithBothKeys() throws Exception {
        PrivateKey ed25519Key = PrivateKey.generateED25519();
        PrivateKey ecdsaKey = PrivateKey.generateECDSA();
        EvmAddress evmAddress = ecdsaKey.getPublicKey().toEvmAddress();

        AccountCreateTransaction transaction = new AccountCreateTransaction()
            .setKeyWithAlias(ed25519Key, ecdsaKey)
            .freezeWith(client);

        transaction.sign(ed25519Key);
        transaction.sign(ecdsaKey);
        TransactionResponse response = transaction.execute(client);

        AccountId accountId = response.getReceipt(client).accountId;
        AccountInfo info = new AccountInfoQuery()
            .setAccountId(accountId)
            .execute(client);

        System.out.println("Account's key: " + info.key + " is the same as " + ed25519Key.getPublicKey());
        System.out.println("Initial EVM address: " + evmAddress + " is the same as " + info.contractAccountId);
    }

    public void createAccountWithoutAlias() throws Exception {
        PrivateKey privateKey = PrivateKey.generateECDSA();

        AccountCreateTransaction transaction = new AccountCreateTransaction()
            .setKeyWithoutAlias(privateKey)
            .freezeWith(client);

        transaction.sign(privateKey);
        TransactionResponse response = transaction.execute(client);

        AccountId accountId = response.getReceipt(client).accountId;
        AccountInfo info = new AccountInfoQuery()
            .setAccountId(accountId)
            .execute(client);

        System.out.println("Account's key: " + info.key + " is the same as " + privateKey.getPublicKey());
        System.out.println("Account has no alias: " + isZeroAddress(Hex.decode(info.contractAccountId)));
    }

    private static boolean isZeroAddress(byte[] address) {
        for (int i = 0; i < 12; i++) {
            if (address[i] != 0) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        try {
            AccountCreateWithAndWithoutAliasExample accountCreateWithAndWithoutAliasExample = new AccountCreateWithAndWithoutAliasExample();

            accountCreateWithAndWithoutAliasExample.createAccountWithAlias();
            accountCreateWithAndWithoutAliasExample.createAccountWithBothKeys();
            accountCreateWithAndWithoutAliasExample.createAccountWithoutAlias();

        } catch (Exception e) {
            System.err.println("Error creating accounts: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
