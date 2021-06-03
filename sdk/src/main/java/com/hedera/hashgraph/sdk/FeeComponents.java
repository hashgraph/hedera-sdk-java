package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;



public class FeeComponents {
    
    
    private long min; // A minimum, the calculated fee must be greater than this value
    private long max; // A maximum, the calculated fee must be less than this value
    private long constant; // A constant contribution to the fee
    private long bpt; // The price of bandwidth consumed by a transaction, measured in bytes
    private long vpt; // The price per signature verification for a transaction
    private long rbh; // The price of RAM consumed by a transaction, measured in byte-hours
    private long sbh; // The price of storage consumed by a transaction, measured in byte-hours
    private long gas; // The price of computation for a smart contract transaction, measured in gas
    private long tv; // The price per hbar transferred for a transfer
    private long bpr; // The price of bandwidth for data retrieved from memory for a response, measured in bytes
    private long sbpr; // The price of bandwidth for data retrieved from disk for a response, measured in bytes



    public FeeComponents() {
        // All members initialized to 0
    }


    // setters

    public FeeComponents setMin(long min) {
        this.min = min;
        return this;
    }
    public FeeComponents setMax(long max) {
        this.max = max;
        return this;
    }
    public FeeComponents setConstant(long constant) {
        this.constant = constant;
        return this;
    }
    public FeeComponents setBpt(long bpt) {
        this.bpt = bpt;
        return this;
    }
    public FeeComponents setVpt(long vpt) {
        this.vpt = vpt;
        return this;
    }
    public FeeComponents setRbh(long rbh) {
        this.rbh = rbh;
        return this;
    }
    public FeeComponents setSbh(long sbh) {
        this.sbh = sbh;
        return this;
    }
    public FeeComponents setGas(long gas) {
        this.gas = gas;
        return this;
    }
    public FeeComponents setTv(long tv) {
        this.tv = tv;
        return this;
    }
    public FeeComponents setBpr(long bpr) {
        this.bpr = bpr;
        return this;
    }
    public FeeComponents setSbpr(long sbpr) {
        this.sbpr = sbpr;
        return this;
    }



    // getters

    public long getMin() {
        return min;
    }
    public long getMax() {
        return max;
    }
    public long getConstant() {
        return constant;
    }
    public long getBpt() {
        return bpt;
    }
    public long getVpt() {
        return vpt;
    }
    public long getRbh() {
        return rbh;
    }
    public long getSbh() {
        return sbh;
    }
    public long getGas() {
        return gas;
    }
    public long getTv() {
        return tv;
    }
    public long getBpr() {
        return bpr;
    }
    public long getSbpr() {
        return sbpr;
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
    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}