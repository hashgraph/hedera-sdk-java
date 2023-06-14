import com.hedera.hashgraph.sdk.*;
import com.hedera.hashgraph.sdk.logger.LogLevel;
import com.hedera.hashgraph.sdk.logger.Logger;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Objects;
import java.util.concurrent.TimeoutException;

public class LoggerFunctionalitiesExample {

    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    public static void main(String[] args) throws PrecheckStatusException, TimeoutException {
        var debugLogger = new Logger(LogLevel.DEBUG);
        var infoLogger = new Logger(LogLevel.INFO);

        Client client = Client
            .forName(HEDERA_NETWORK)
            .setLogger(debugLogger)
            .setOperator(OPERATOR_ID, OPERATOR_KEY);

        var privateKey = PrivateKey.generateED25519();
        var publicKey = privateKey.getPublicKey();
        var aliasAccountId = publicKey.toAccountId(0, 0);

//        new TransferTransaction()
//            .addHbarTransfer(client.getOperatorAccountId(), Hbar.fromTinybars(-10))
//            .addHbarTransfer(aliasAccountId, Hbar.fromTinybars(10))
//            .setTransactionMemo("")
//            .execute(client);

        new TopicCreateTransaction()
            .setLogger(infoLogger)
            .setTopicMemo("topic memo")
            .execute(client);

        // Set the level of the `infoLogger` from `info` to `error`
        infoLogger.setLevel(LogLevel.ERROR);

        // This should not display any logs because currently there are no `warn` logs predefined in the SDK
        new TopicCreateTransaction()
            .setLogger(infoLogger)
            .setTopicMemo("topic memo")
            .execute(client);

        // Silence the `debugLogger` - no logs should be shown
        // This can also be achieved by calling `.setLevel(LogLevel.Silent)`
        debugLogger.setSilent(true);

        new TopicCreateTransaction()
            .setLogger(debugLogger)
            .setTopicMemo("topic memo")
            .execute(client);

        // Unsilence the `debugLogger` - applies back the old log level before silencing
        debugLogger.setSilent(false);

        new TopicCreateTransaction()
            .setLogger(debugLogger)
            .setTopicMemo("topicMemo")
            .execute(client);
    }
}
