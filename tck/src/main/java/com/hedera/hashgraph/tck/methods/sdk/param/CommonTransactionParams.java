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
        var maxTransactionFee = Optional.ofNullable((Long) jrpcParams.get("maxTransactionFee"));
        var validTransactionDuration = Optional.ofNullable((Long) jrpcParams.get("validTransactionDuration"));
        var memo = Optional.ofNullable((String) jrpcParams.get("memo"));
        var regenerateTransactionId = Optional.ofNullable((Boolean) jrpcParams.get("regenerateTransactionId"));

        // TODO: double check it
        var signers = Optional.ofNullable((List<String>) jrpcParams.get("signers"));

        return new CommonTransactionParams(
                maxTransactionFee, validTransactionDuration, memo, regenerateTransactionId, signers);
    }

    public void fillOutTransaction(final Transaction transaction, final Client client) {
        maxTransactionFee.ifPresent(v -> transaction.setMaxTransactionFee(Hbar.from(v)));
        validTransactionDuration.ifPresent(v -> transaction.setTransactionValidDuration(Duration.ofSeconds(v)));
        memo.ifPresent(transaction::setTransactionMemo);
        regenerateTransactionId.ifPresent(transaction::setRegenerateTransactionId);
        signers.ifPresent(signers -> {
            transaction.freezeWith(client);
            signers.forEach(signer -> {
                var pk = PrivateKey.fromString(signer);
                transaction.sign(pk);
            });
        });
    }
}
