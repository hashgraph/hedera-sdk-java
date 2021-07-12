package com.hedera.hashgraph.sdk;

public enum NetworkName {
    MAINNET(0),
    TESTNET(1),
    PREVIEWNET(2);

    final int id;

    NetworkName(int id) {
        this.id = id;
    }

    public static NetworkName fromString(String networkName) {
        switch (networkName) {
            case "mainnet":
                return NetworkName.MAINNET;
            case "testnet":
                return NetworkName.TESTNET;
            case "previewnet":
                return NetworkName.PREVIEWNET;
            default:
                throw new IllegalArgumentException("The only supported network names are 'mainnet', 'testnet', and 'previewnet'");
        }
    }

    @Override
    public String toString() {
        switch (this) {
            case MAINNET:
                return "mainnet";
            case TESTNET:
                return "testnet";
            case PREVIEWNET:
                return "previewnet";
            default:
                throw new IllegalStateException("(BUG) `NetworkName.toString()` switch is non-exhaustive");
        }
    }
}
