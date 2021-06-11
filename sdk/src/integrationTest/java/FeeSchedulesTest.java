import com.hedera.hashgraph.sdk.FeeSchedules;
import com.hedera.hashgraph.sdk.FileContentsQuery;
import com.hedera.hashgraph.sdk.FileId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.google.protobuf.ByteString;

import static org.junit.jupiter.api.Assertions.*;

public class FeeSchedulesTest {
    @Test
    @DisplayName("FeeSchedules (CurrentAndNextFeeSchedule) is fetched and parsed from file 0.0.111")
    void canFetchFeeSchedules() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            ByteString feeSchedulesBytes = new FileContentsQuery()
                .setFileId(new FileId(0, 0, 111))
                .execute(testEnv.client);
            
            FeeSchedules feeSchedules = FeeSchedules.fromBytes(feeSchedulesBytes.toByteArray());
            assertNotNull(feeSchedules.getCurrent());
            testEnv.client.close();
        });
    }
}