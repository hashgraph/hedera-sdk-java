package com.hedera.hashgraph.sdk;

/**
 * Simplified representation of the 16 bit half of an IPv4Address.
 */
public class IPv4AddressPart {
    /**
     * Represents the first byte.
     */
    byte left;
    /**
     * Represents the last byte.
     */
    byte right;

    /**
     * Constructor.
     */
    IPv4AddressPart() {
    }

    /**
     * @return                          the first byte
     */
    public byte getLeft() {
        return left;
    }

    /**
     * Assign the first byte.
     *
     * @param left                      the first byte
     * @return {@code this}
     */
    public IPv4AddressPart setLeft(byte left) {
        this.left = left;
        return this;
    }

    /**
     * @return                          the last byte
     */
    public byte getRight() {
        return right;
    }

    /**
     * Assign the last byte.
     *
     * @param right                     the last byte
     * @return {@code this}
     */
    public IPv4AddressPart setRight(byte right) {
        this.right = right;
        return this;
    }

    @Override
    public String toString() {
        return ((int) left & 0x000000FF) + "." + ((int) right & 0x000000FF);
    }
}
