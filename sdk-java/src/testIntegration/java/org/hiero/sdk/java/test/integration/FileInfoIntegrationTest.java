// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk.java.test.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Objects;
import org.hiero.sdk.java.FileCreateTransaction;
import org.hiero.sdk.java.FileDeleteTransaction;
import org.hiero.sdk.java.FileInfoQuery;
import org.hiero.sdk.java.Hbar;
import org.hiero.sdk.java.KeyList;
import org.hiero.sdk.java.MaxQueryPaymentExceededException;
import org.hiero.sdk.java.PrecheckStatusException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class FileInfoIntegrationTest {
    @Test
    @DisplayName("Can query file info")
    void canQueryFileInfo() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            var response = new FileCreateTransaction()
                    .setKeys(testEnv.operatorKey)
                    .setContents("[e2e::FileCreateTransaction]")
                    .execute(testEnv.client);

            var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

            var info = new FileInfoQuery().setFileId(fileId).execute(testEnv.client);

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
        }
    }

    @Test
    @DisplayName("Can query file info with no admin key or contents")
    void canQueryFileInfoWithNoAdminKeyOrContents() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            var response = new FileCreateTransaction().execute(testEnv.client);

            var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

            var info = new FileInfoQuery().setFileId(fileId).execute(testEnv.client);

            assertThat(info.fileId).isEqualTo(fileId);
            assertThat(info.size).isEqualTo(0);
            assertThat(info.isDeleted).isFalse();
            assertThat(info.keys).isNull();
        }
    }

    @Test
    @DisplayName("Can get cost, even with a big max")
    @SuppressWarnings("UnusedVariable")
    void getCostBigMaxQueryFileInfo() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            var response = new FileCreateTransaction()
                    .setKeys(testEnv.operatorKey)
                    .setContents("[e2e::FileCreateTransaction]")
                    .execute(testEnv.client);

            var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

            var infoQuery = new FileInfoQuery().setFileId(fileId).setMaxQueryPayment(new Hbar(1000));

            var cost = infoQuery.getCost(testEnv.client);

            var info = infoQuery.setQueryPayment(cost).execute(testEnv.client);

            new FileDeleteTransaction()
                    .setFileId(fileId)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
        }
    }

    @Test
    @DisplayName("Error, max is smaller than set payment.")
    void getCostSmallMaxQueryFileInfo() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            var response = new FileCreateTransaction()
                    .setKeys(testEnv.operatorKey)
                    .setContents("[e2e::FileCreateTransaction]")
                    .execute(testEnv.client);

            var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

            var infoQuery = new FileInfoQuery().setFileId(fileId).setMaxQueryPayment(Hbar.fromTinybars(1));

            assertThatExceptionOfType(MaxQueryPaymentExceededException.class).isThrownBy(() -> {
                infoQuery.execute(testEnv.client);
            });

            new FileDeleteTransaction()
                    .setFileId(fileId)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
        }
    }

    @Test
    @DisplayName("Insufficient tx fee error.")
    void getCostInsufficientTxFeeQueryFileInfo() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            var response = new FileCreateTransaction()
                    .setKeys(testEnv.operatorKey)
                    .setContents("[e2e::FileCreateTransaction]")
                    .execute(testEnv.client);

            var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

            var infoQuery = new FileInfoQuery().setFileId(fileId).setMaxQueryPayment(Hbar.fromTinybars(1));

            assertThatExceptionOfType(PrecheckStatusException.class)
                    .isThrownBy(() -> {
                        infoQuery.setQueryPayment(Hbar.fromTinybars(1)).execute(testEnv.client);
                    })
                    .satisfies(error -> assertThat(error.status.toString()).isEqualTo("INSUFFICIENT_TX_FEE"));

            new FileDeleteTransaction()
                    .setFileId(fileId)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
        }
    }
}
