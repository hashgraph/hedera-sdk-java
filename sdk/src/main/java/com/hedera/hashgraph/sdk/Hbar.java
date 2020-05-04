package com.hedera.hashgraph.sdk;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

/**
 * Represents a quantity of hbar.
 * <p>
 * Implemented as a wrapper class to force handling of units. Direct interfacing with Hedera accepts amounts
 * in tinybars however the nominal unit is hbar.
 */
public final class Hbar implements Comparable<Hbar> {
    private final long valueInTinybar;

    /**
     * Constructs a new Hbar of the specified value.
     */
    public Hbar(long amount) {
        this(amount, HbarUnit.HBAR);
    }

    Hbar(long amount, HbarUnit unit) {
        valueInTinybar = amount * unit.tinybar;
    }

    /**
     * Constructs a new Hbar of the specified, possibly fractional value.
     * <p>
     * The equivalent amount in tinybar must be an integer and fit in a {@code long} (64-bit signed integer).
     * <p>
     * E.g., {@code 1.23456789} is a valid amount of hbar but {@code 0.123456789} is not.
     */
    public Hbar(BigDecimal amount) {
        this(amount, HbarUnit.HBAR);
    }

    Hbar(BigDecimal amount, HbarUnit unit) {
        valueInTinybar = amount.multiply(BigDecimal.valueOf(unit.tinybar)).longValue();
    }

    /**
     * A constant value of zero hbars.
     */
    public static final Hbar ZERO = Hbar.fromTinybars(0);

    /**
     * A constant value of the maximum number of hbars.
     */
    public static final Hbar MAX = Hbar.from(50_000_000_000L);

    /**
     * A constant value of the minimum number of hbars.
     */
    public static final Hbar MIN = Hbar.from(-50_000_000_000L);

    /**
     * Converts the provided string into an amount of hbars.
     */
    public static Hbar fromString(CharSequence text) {
        return new Hbar(new BigDecimal(text.toString()), HbarUnit.HBAR);
    }

    /**
     * Converts the provided string into an amount of hbars.
     */
    public static Hbar fromString(CharSequence text, HbarUnit unit) {
        return new Hbar(new BigDecimal(text.toString()), unit);
    }

    /**
     * Returns an Hbar whose value is equal to the specified long.
     */
    public static Hbar from(long hbars) {
        return new Hbar(hbars, HbarUnit.HBAR);
    }

    /**
     * Returns an Hbar representing the value in the given units.
     */
    public static Hbar from(long amount, HbarUnit unit) {
        return new Hbar(amount, unit);
    }

    /**
     * Returns an Hbar whose value is equal to the specified long.
     */
    public static Hbar from(BigDecimal hbars) {
        return new Hbar(hbars, HbarUnit.HBAR);
    }

    /**
     * Returns an Hbar representing the value in the given units.
     */
    public static Hbar from(BigDecimal amount, HbarUnit unit) {
        return new Hbar(amount, unit);
    }

    /**
     * Returns an Hbar converted from the specified number of tinybars.
     */
    public static Hbar fromTinybars(long tinybars) {
        return new Hbar(tinybars, HbarUnit.TINYBAR);
    }

    /**
     * Convert this hbar value to a different unit.
     */
    public BigDecimal to(HbarUnit unit) {
        return BigDecimal.valueOf(valueInTinybar).divide(BigDecimal.valueOf(unit.tinybar), MathContext.UNLIMITED);
    }

    /**
     * Convert this hbar value to Tinybars.
     */
    public long toTinybars() {
        return valueInTinybar;
    }

    /**
     * Returns the number of Hbars.
     */
    public BigDecimal getValue() {
        return to(HbarUnit.HBAR);
    }

    /**
     * Returns a Hbar whose value is {@code -this}.
     */
    public Hbar negate() {
        return Hbar.fromTinybars(-valueInTinybar);
    }

    @Override
    public String toString() {
        if (valueInTinybar < 10_000) {
            return Long.toString(this.valueInTinybar) + " " + HbarUnit.TINYBAR.getSymbol();
        }

        return to(HbarUnit.HBAR).toString() + " " + HbarUnit.HBAR.getSymbol();
    }

    public String toString(HbarUnit unit) {
        return to(unit).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hbar hbar = (Hbar) o;
        return valueInTinybar == hbar.valueInTinybar;
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueInTinybar);
    }

    @Override
    public int compareTo(Hbar o) {
        return Long.compare(valueInTinybar, o.valueInTinybar);
    }
}
