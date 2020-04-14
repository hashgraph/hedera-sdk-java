package com.hedera.hashgraph.sdk;

/**
 * Common units of hbar; for the most part they follow SI prefix conventions.
 */
public enum HbarUnit {
    /**
     * The atomic (smallest) unit of hbar, used natively by the Hedera network.
     *
     * It is equivalent to <sup>1</sup>&frasl;<sub>100,000,000</sub> hbar.
     */
    Tinybar("tℏ", 1),
    /**
     * Equivalent to 100 tinybar or <sup>1</sup>&frasl;<sub>1,000,000</sub> hbar.
     */
    Microbar("μℏ", 100),
    /**
     * Equivalent to 100,000 tinybar or <sup>1</sup>&frasl;<sub>1,000</sub> hbar.
     */
    Millibar("mℏ", 100_000),
    /**
     * The base unit of hbar, equivalent to 100 million tinybar.
     */
    Hbar("ℏ", 100_000_000),
    /**
     * Equivalent to 1 thousand hbar or 100 billion tinybar.
     */
    Kilobar("kℏ", 1000 * 100_000_000L),
    /**
     * Equivalent to 1 million hbar or 100 trillion tinybar.
     */
    Megabar("Mℏ", 1_000_000 * 100_000_000L),
    /**
     * Equivalent to 1 billion hbar or 100 quadillion tinybar.
     *
     * The maximum hbar amount supported by Hedera in any context is ~92 gigabar
     * (2<sup>63</sup> tinybar); use this unit sparingly.
     */
    Gigabar("Gℏ", 1_000_000_000 * 100_000_000L);

    private final String symbol;
    final long tinybar;

    HbarUnit(String symbol, long tinybar) {
        this.symbol = symbol;
        this.tinybar = tinybar;
    }

    /**
     * Get the preferred symbol of the current unit.
     *
     * E.g. {@link #Tinybar}.getSymbol() returns "tℏ".
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Get the name of this unit.
     */
    @Override public String toString() {
        return name().toLowerCase();
    }
}
