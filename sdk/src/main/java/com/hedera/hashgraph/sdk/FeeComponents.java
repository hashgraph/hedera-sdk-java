package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.common.base.MoreObjects;

public class FeeComponents {
    /*
    * A minimum, the calculated fee must be greater than this value
    */
    private long min;
    /*
    * A maximum, the calculated fee must be less than this value
    */
    private long max;
    /*
    * A constant contribution to the fee
    */
    private long constant;
    /*
    * The price of bandwidth consumed by a transaction, measured in bytes
    */
    private long transactionBandwidthByte;
    /*
    * The price per signature verification for a transaction
    */
    private long transactionVerification;
    /*
    * The price of RAM consumed by a transaction, measured in byte-hours
    */
    private long transactionRamByteHour;
    /*
    * The price of storage consumed by a transaction, measured in byte-hours
    */
    private long transactionStorageByteHour;
    /*
    * The price of computation for a smart contract transaction, measured in gas
    */
    private long contractTransactionGas;
    /*
    * The price per hbar transferred for a transfer
    */
    private long transferVolumeHbar;
    /*
    * The price of bandwidth for data retrieved from memory for a response, measured in bytes
    */
    private long responseMemoryByte;
    /*
    * The price of bandwidth for data retrieved from disk for a response, measured in bytes
    */
    private long responseDiskByte;

    public FeeComponents() {
    }

    static FeeComponents fromProtobuf(com.hedera.hashgraph.sdk.proto.FeeComponents feeComponents) {
        return new FeeComponents()
            .setMin(feeComponents.getMin())
            .setMax(feeComponents.getMax())
            .setConstant(feeComponents.getConstant())
            .setTransactionBandwidthByte(feeComponents.getBpt())
            .setTransactionVerification(feeComponents.getVpt())
            .setTransactionRamByteHour(feeComponents.getRbh())
            .setTransactionStorageByteHour(feeComponents.getSbh())
            .setContractTransactionGas(feeComponents.getGas())
            .setTransferVolumeHbar(feeComponents.getTv())
            .setResponseMemoryByte(feeComponents.getBpr())
            .setResponseDiskByte(feeComponents.getSbpr());
    }

    public static FeeComponents fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.FeeComponents.parseFrom(bytes).toBuilder().build());
    }

    public long getMin() {
        return min;
    }

    public FeeComponents setMin(long min) {
        this.min = min;
        return this;
    }

    public long getMax() {
        return max;
    }

    public FeeComponents setMax(long max) {
        this.max = max;
        return this;
    }

    public long getConstant() {
        return constant;
    }

    public FeeComponents setConstant(long constant) {
        this.constant = constant;
        return this;
    }

    public long getTransactionBandwidthByte() {
        return transactionBandwidthByte;
    }

    public FeeComponents setTransactionBandwidthByte(long transactionBandwidthByte) {
        this.transactionBandwidthByte = transactionBandwidthByte;
        return this;
    }

    public long getTransactionVerification() {
        return transactionVerification;
    }

    public FeeComponents setTransactionVerification(long transactionVerification) {
        this.transactionVerification = transactionVerification;
        return this;
    }

    public long getTransactionRamByteHour() {
        return transactionRamByteHour;
    }

    public FeeComponents setTransactionRamByteHour(long transactionRamByteHour) {
        this.transactionRamByteHour = transactionRamByteHour;
        return this;
    }

    public long getTransactionStorageByteHour() {
        return transactionStorageByteHour;
    }

    public FeeComponents setTransactionStorageByteHour(long transactionStorageByteHour) {
        this.transactionStorageByteHour = transactionStorageByteHour;
        return this;
    }

    public long getContractTransactionGas() {
        return contractTransactionGas;
    }

    public FeeComponents setContractTransactionGas(long contractTransactionGas) {
        this.contractTransactionGas = contractTransactionGas;
        return this;
    }

    public long getTransferVolumeHbar() {
        return transferVolumeHbar;
    }

    public FeeComponents setTransferVolumeHbar(long transferVolumeHbar) {
        this.transferVolumeHbar = transferVolumeHbar;
        return this;
    }

    public long getResponseMemoryByte() {
        return responseMemoryByte;
    }

    public FeeComponents setResponseMemoryByte(long responseMemoryByte) {
        this.responseMemoryByte = responseMemoryByte;
        return this;
    }

    public long getResponseDiskByte() {
        return responseDiskByte;
    }

    public FeeComponents setResponseDiskByte(long responseDiskByte) {
        this.responseDiskByte = responseDiskByte;
        return this;
    }

    com.hedera.hashgraph.sdk.proto.FeeComponents toProtobuf()
    {
        return com.hedera.hashgraph.sdk.proto.FeeComponents.newBuilder()
            .setMin(getMin())
            .setMax(getMax())
            .setConstant(getConstant())
            .setBpt(getTransactionBandwidthByte())
            .setVpt(getTransactionVerification())
            .setRbh(getTransactionRamByteHour())
            .setSbh(getTransactionStorageByteHour())
            .setGas(getContractTransactionGas())
            .setTv(getTransferVolumeHbar())
            .setBpr(getResponseMemoryByte())
            .setSbpr(getResponseDiskByte())
            .build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("min", getMin())
            .add("max", getMax())
            .add("constant", getConstant())
            .add("transactionBandwidthByte", getTransactionBandwidthByte())
            .add("transactionVerification", getTransactionVerification())
            .add("transactionRamByteHour", getTransactionRamByteHour())
            .add("transactionStorageByteHour", getTransactionStorageByteHour())
            .add("contractTransactionGas", getContractTransactionGas())
            .add("transferVolumeHbar", getTransferVolumeHbar())
            .add("responseMemoryByte", getResponseMemoryByte())
            .add("responseDiskByte", getResponseDiskByte())
            .toString();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
