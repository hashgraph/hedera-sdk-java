package com.hedera.hashgraph.tck.methods.sdk.param;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.Transaction;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * CommonTransactionParams
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CommonTransactionParams {
    private Optional<Long> maxTransactionFee;
    private Optional<Long> validTransactionDuration;
    private Optional<String> memo;
    private Optional<Boolean> regenerateTransactionId;
    private Optional<List<String>> signers;

    public static CommonTransactionParams parse(Map<String, Object> jrpcParams) throws ClassCastException {
        var parsedMaxTransactionFee = Optional.ofNullable((Long) jrpcParams.get("maxTransactionFee"));
        var parsedValidTransactionDuration = Optional.ofNullable((Long) jrpcParams.get("validTransactionDuration"));
        var parsedMemo = Optional.ofNullable((String) jrpcParams.get("memo"));
        var parsedRegenerateTransactionId = Optional.ofNullable((Boolean) jrpcParams.get("regenerateTransactionId"));

        // TODO: double check it
        var signers = Optional.ofNullable((List<String>) jrpcParams.get("signers"));

        return new CommonTransactionParams(
                parsedMaxTransactionFee,
                parsedValidTransactionDuration,
                parsedMemo,
                parsedRegenerateTransactionId,
                signers);
    }

    public void fillOutTransaction(final Transaction<?> transaction, final Client client) {
        maxTransactionFee.ifPresent(v -> transaction.setMaxTransactionFee(Hbar.fromTinybars(v)));
        validTransactionDuration.ifPresent(v -> transaction.setTransactionValidDuration(Duration.ofSeconds(v)));
        memo.ifPresent(transaction::setTransactionMemo);
        regenerateTransactionId.ifPresent(transaction::setRegenerateTransactionId);
        signers.ifPresent(s -> {
            transaction.freezeWith(client);
            s.forEach(signer -> {
                var pk = PrivateKey.fromString(signer);
                transaction.sign(pk);
            });
        });
    }
}
