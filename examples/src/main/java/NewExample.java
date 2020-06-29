import com.hedera.hashgraph.sdk.*;
import org.threeten.bp.Instant;

import java.util.concurrent.TimeoutException;

public class NewExample {

    private static final PrivateKey unusedPrivateKey = PrivateKey.fromString(
        "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10");

    private NewExample() {
    }



    public static void main(String[] args) throws TimeoutException, HederaPreCheckStatusException, HederaReceiptStatusException {
        Instant validStart = Instant.ofEpochSecond(1554158542);

        var tx = new FreezeTransaction()
            .setNodeAccountId(AccountId.fromString("0.0.5005"))
            .setTransactionId(new TransactionId(AccountId.fromString("0.0.5006"), validStart))
            .setStartTime(0, 0)
            .setEndTime(23, 59)
            .setMaxTransactionFee(Hbar.fromTinybars(100_000))
            .build(Client.forTestnet());

        System.out.println(tx.toString().replaceAll("(?m)^# com.hedera.hashgraph.sdk.proto.Transaction.*", ""));
    }
}
