package com.hedera.hashgraph.sdk;

import java.util.Arrays;

import java8.util.concurrent.CompletableFuture;

import java.util.concurrent.TimeoutException;
import javax.annotation.Nullable;

public class EthereumFlow implements WithExecute<TransactionResponse> {
    /**
     * 5KiB in Bytes
     * Indicates when we should splice out the call data from an ethereum transaction data
     */
    static int MAX_ETHEREUM_DATA_SIZE = 5120;
    @Nullable
    private EthereumTransactionData ethereumData;

    @Nullable
    private FileId callDataFileId;

    @Nullable
    private Hbar maxGasAllowance;


    public EthereumFlow() {
    }

    @Nullable
    public EthereumTransactionData getEthereumData() {
        return ethereumData;
    }

    public EthereumFlow setEthereumData(byte[] ethereumData) {
        this.ethereumData = EthereumTransactionData.fromBytes(ethereumData);
        return this;
    }

    @Nullable
    public Hbar getMaxGasAllowance() {
        return maxGasAllowance;
    }

    public EthereumFlow setMaxGasAllowance(Hbar maxGasAllowance) {
        this.maxGasAllowance = maxGasAllowance;
        return this;
    }

    private static FileId createFile(byte[] callData, Client client) throws PrecheckStatusException, TimeoutException {
        try {
            var transaction = new FileCreateTransaction()
                    .setContents(Arrays.copyOfRange(callData, 0, Math.min(FileAppendTransaction.DEFAULT_CHUNK_SIZE, callData.length)))
                    .execute(client);
            var fileId = transaction.getReceipt(client).fileId;

            if (callData.length > FileAppendTransaction.DEFAULT_CHUNK_SIZE) {
                new FileAppendTransaction()
                        .setFileId(fileId)
                        .setContents(Arrays.copyOfRange(callData, FileAppendTransaction.DEFAULT_CHUNK_SIZE, callData.length))
                        .execute(client);
            }

            return fileId;
        } catch (ReceiptStatusException e) {
            throw new RuntimeException(e);
        }

    }

    private static CompletableFuture<FileId> createFileAsync(byte[] callData, Client client) {
        return new FileCreateTransaction()
                .setContents(Arrays.copyOfRange(callData, 0, Math.min(FileAppendTransaction.DEFAULT_CHUNK_SIZE, callData.length)))
                .executeAsync(client)
                .thenCompose((response) -> response.getReceiptAsync(client))
                .thenCompose((receipt) -> {
                    if (callData.length > FileAppendTransaction.DEFAULT_CHUNK_SIZE) {
                        return new FileAppendTransaction()
                                .setFileId(receipt.fileId)
                                .setContents(Arrays.copyOfRange(callData, FileAppendTransaction.DEFAULT_CHUNK_SIZE, callData.length))
                                .executeAsync(client)
                                .thenApply((r) -> receipt.fileId);
                    } else {
                        return CompletableFuture.completedFuture(receipt.fileId);
                    }
                });
    }

    public TransactionResponse execute(Client client) throws PrecheckStatusException, TimeoutException {
        if (ethereumData == null) {
            throw new IllegalStateException("Cannot execute a ethereum flow when ethereum data was not provided");
        }

        var ethereumTransaction = new EthereumTransaction();
        var ethereumDataBytes = ethereumData.toBytes();

        if (maxGasAllowance != null) {
            ethereumTransaction.setMaxGasAllowanceHbar(maxGasAllowance);
        }

        if (ethereumDataBytes.length <= MAX_ETHEREUM_DATA_SIZE) {
            ethereumTransaction.setEthereumData(ethereumDataBytes);
        } else {
            var callDataFileId = createFile(ethereumData.callData, client);
            ethereumData.callData = new byte[]{};
            ethereumTransaction.setEthereumData(ethereumData.toBytes()).setCallDataFileId(callDataFileId);
        }

        return ethereumTransaction.execute(client);
    }


    public CompletableFuture<TransactionResponse> executeAsync(Client client) {
        if (ethereumData == null) {
            CompletableFuture.failedFuture(new IllegalStateException("Cannot execute a ethereum flow when ethereum data was not provided"));
        }

        var ethereumTransaction = new EthereumTransaction();
        var ethereumDataBytes = ethereumData.toBytes();

        if (maxGasAllowance != null) {
            ethereumTransaction.setMaxGasAllowanceHbar(maxGasAllowance);
        }

        if (ethereumDataBytes.length <= MAX_ETHEREUM_DATA_SIZE) {
            return ethereumTransaction.setEthereumData(ethereumDataBytes).executeAsync(client);
        } else {
            return createFileAsync(ethereumData.callData, client)
                    .thenCompose((callDataFileId) -> {
                        ethereumData.callData = new byte[]{};
                        return ethereumTransaction
                                .setEthereumData(ethereumData.toBytes())
                                .setCallDataFileId(callDataFileId)
                                .executeAsync(client);
                    });
        }
    }
}
