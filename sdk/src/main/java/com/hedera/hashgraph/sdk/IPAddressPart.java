package com.hedera.hashgraph.sdk;

class IPv4AddressPart {
    byte left;
    byte right;

    IPv4AddressPart() {
    }

    public byte getLeft() {
        return left;
    }

    public IPv4AddressPart setLeft(byte left) {
        this.left = left;
        return this;
    }

    public byte getRight() {
        return right;
    }

    public IPv4AddressPart setRight(byte right) {
        this.right = right;
        return this;
    }

    public String toString() {
        return Byte.toUnsignedInt(left) + "." + Byte.toUnsignedInt(right);
    }
}
