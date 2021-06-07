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
    private long bpt;
    /*
    * The price per signature verification for a transaction
    */
    private long vpt;
    /*
    * The price of RAM consumed by a transaction, measured in byte-hours
    */
    private long rbh;
    /*
    * The price of storage consumed by a transaction, measured in byte-hours
    */
    private long sbh;
    /*
    * The price of computation for a smart contract transaction, measured in gas
    */
    private long gas;
    /*
    * The price per hbar transferred for a transfer
    */
    private long tv;
    /*
    * The price of bandwidth for data retrieved from memory for a response, measured in bytes
    */
    private long bpr;
    /*
    * The price of bandwidth for data retrieved from disk for a response, measured in bytes
    */
    private long sbpr;

    public FeeComponents() {
    }

    static FeeComponents fromProtobuf(com.hedera.hashgraph.sdk.proto.FeeComponents feeComponents) {
        return new FeeComponents()
            .setMin(feeComponents.getMin())
            .setMax(feeComponents.getMax())
            .setConstant(feeComponents.getConstant())
            .setBpt(feeComponents.getBpt())
            .setVpt(feeComponents.getVpt())
            .setRbh(feeComponents.getRbh())
            .setSbh(feeComponents.getSbh())
            .setGas(feeComponents.getGas())
            .setTv(feeComponents.getTv())
            .setBpr(feeComponents.getBpr())
            .setSbpr(feeComponents.getSbpr());
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

    public long getBpt() {
        return bpt;
    }

    public FeeComponents setBpt(long bpt) {
        this.bpt = bpt;
        return this;
    }

    public long getVpt() {
        return vpt;
    }

    public FeeComponents setVpt(long vpt) {
        this.vpt = vpt;
        return this;
    }

    public long getRbh() {
        return rbh;
    }

    public FeeComponents setRbh(long rbh) {
        this.rbh = rbh;
        return this;
    }

    public long getSbh() {
        return sbh;
    }

    public FeeComponents setSbh(long sbh) {
        this.sbh = sbh;
        return this;
    }

    public long getGas() {
        return gas;
    }

    public FeeComponents setGas(long gas) {
        this.gas = gas;
        return this;
    }

    public long getTv() {
        return tv;
    }

    public FeeComponents setTv(long tv) {
        this.tv = tv;
        return this;
    }

    public long getBpr() {
        return bpr;
    }

    public FeeComponents setBpr(long bpr) {
        this.bpr = bpr;
        return this;
    }

    public long getSbpr() {
        return sbpr;
    }

    public FeeComponents setSbpr(long sbpr) {
        this.sbpr = sbpr;
        return this;
    }

    com.hedera.hashgraph.sdk.proto.FeeComponents toProtobuf()
    {
        return com.hedera.hashgraph.sdk.proto.FeeComponents.newBuilder()
            .setMin(getMin())
            .setMax(getMax())
            .setConstant(getConstant())
            .setBpt(getBpt())
            .setVpt(getVpt())
            .setRbh(getRbh())
            .setSbh(getSbh())
            .setGas(getGas())
            .setTv(getTv())
            .setBpr(getBpr())
            .setSbpr(getSbpr())
            .build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("min", getMin())
            .add("max", getMax())
            .add("constant", getConstant())
            .add("bpt", getBpt())
            .add("vpt", getVpt())
            .add("rbh", getRbh())
            .add("sbh", getSbh())
            .add("gas", getGas())
            .add("tv", getTv())
            .add("bpr", getBpr())
            .add("sbpr", getSbpr())
            .toString();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}