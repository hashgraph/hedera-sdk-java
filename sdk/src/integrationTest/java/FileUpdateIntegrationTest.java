import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.FileCreateTransaction;
import com.hedera.hashgraph.sdk.FileDeleteTransaction;
import com.hedera.hashgraph.sdk.FileInfoQuery;
import com.hedera.hashgraph.sdk.FileUpdateTransaction;
import com.hedera.hashgraph.sdk.KeyList;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class FileUpdateIntegrationTest {
    @Test
    @DisplayName("Can update file")
    void canUpdateFile() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var response = new FileCreateTransaction()
            .setKeys(testEnv.operatorKey)
            .setContents("[e2e::FileCreateTransaction]")
            .execute(testEnv.client);

        var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

        @Var var info = new FileInfoQuery()
            .setFileId(fileId)
            .execute(testEnv.client);

        assertThat(info.fileId).isEqualTo(fileId);
        assertThat(info.size).isEqualTo(28);
        assertThat(info.isDeleted).isFalse();
        assertThat(info.keys).isNotNull();
        assertThat(info.keys.getThreshold()).isNull();
        assertThat(info.keys).isEqualTo(KeyList.of(testEnv.operatorKey));

        new FileUpdateTransaction()
            .setFileId(fileId)
            .setContents("[e2e::FileUpdateTransaction]")
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        info = new FileInfoQuery()
            .setFileId(fileId)
            .execute(testEnv.client);

        assertThat(info.fileId).isEqualTo(fileId);
        assertThat(info.size).isEqualTo(28);
        assertThat(info.isDeleted).isFalse();
        assertThat(info.keys).isNotNull();
        assertThat(info.keys.getThreshold()).isNull();
        assertThat(info.keys).isEqualTo(KeyList.of(testEnv.operatorKey));

        new FileDeleteTransaction()
            .setFileId(fileId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close();
    }

    @Test
    @DisplayName("Cannot update immutable file")
    void cannotUpdateImmutableFile() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var response = new FileCreateTransaction()
            .setContents("[e2e::FileCreateTransaction]")
            .execute(testEnv.client);

        var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

        var info = new FileInfoQuery()
            .setFileId(fileId)
            .execute(testEnv.client);

        assertThat(info.fileId).isEqualTo(fileId);
        assertThat(info.size).isEqualTo(28);
        assertThat(info.isDeleted).isFalse();
        assertThat(info.keys).isNull();

        assertThatExceptionOfType(ReceiptStatusException.class).isThrownBy(() -> {
            new FileUpdateTransaction()
                .setFileId(fileId)
                .setContents("[e2e::FileUpdateTransaction]")
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        }).withMessageContaining(Status.UNAUTHORIZED.toString());

        testEnv.close();
    }

    @Test
    @DisplayName("Cannot update file when file ID is not set")
    void cannotUpdateFileWhenFileIDIsNotSet() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        assertThatExceptionOfType(ReceiptStatusException.class).isThrownBy(() -> {
            new FileUpdateTransaction()
                .setContents("[e2e::FileUpdateTransaction]")
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        }).withMessageContaining(Status.INVALID_FILE_ID.toString());

        testEnv.close();
    }
}
