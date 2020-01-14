package com.hedera.hashgraph.sdk;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

/**
 * Typesafe wrapper for values of hbar providing foolproof conversions to other denominations.
 * <p>
 * May be positive, negative or zero.
 */
public final class Hbar implements Comparable<Hbar> {
    private final long tinybar;
    private final HbarUnit originalUnit;

    /**
     * Wrap some amount of hbar.
     *
     * @param amount the amount in hbar, may be negative.
     * @throws HbarRangeException if the tinybar equivalent does not fit in a {@code long}.
     */
    public Hbar(long amount) {
        this.tinybar = Hbar.from(amount, HbarUnit.Hbar).tinybar;
        this.originalUnit = HbarUnit.Hbar;
    }

    /**
     * Wrap a possibly fractional amount of hbar.
     * <p>
     * The equivalent amount in tinybar must be an integer and fit in a {@code long}
     * (64-bit signed integer) as that is required by the Hedera network.
     * <p>
     * E.g. 1.23456789 is a valid amount of hbar but 0.123456789 is not.
     *
     * @param amount the amount in hbar, may be fractional and/or negative.
     * @throws HbarRangeException if the tinybar equivalent is not an integer
     *                            or does not fit in a {@code long}.
     */
    public Hbar(BigDecimal amount) {
        this.tinybar = Hbar.from(amount, HbarUnit.Hbar).tinybar;
        this.originalUnit = HbarUnit.Hbar;
    }

    private Hbar(long tinybar, HbarUnit originalUnit) {
        this.tinybar = tinybar;
        this.originalUnit = originalUnit;
    }

    /**
     * Singleton value representing zero hbar.
     */
    public static final Hbar ZERO = new Hbar(0, HbarUnit.Hbar);

    /**
     * Singleton value for the minimum (negative) value this wrapper may contain.
     */
    public static final Hbar MIN = new Hbar(Long.MIN_VALUE, HbarUnit.Hbar);

    /**
     * Singleton value for the maximum (positive) value this wrapper may contain.
     */
    public static final Hbar MAX = new Hbar(Long.MAX_VALUE, HbarUnit.Hbar);

    /**
     * Calculate an hbar amount given a value and a unit to interpret
     * it as.
     * <p>
     * The equivalent amount in tinybar must fit in a {@code long} (64-bit signed integer)
     * as that is required by the Hedera network.
     *
     * @param amount the amount in the given unit, may be negative.
     * @param unit   the unit to multiply the amount by.
     * @return the calculated hbar value.
     * @throws HbarRangeException if the tinybar equivalent does not fit in a {@code long}.
     */
    public static Hbar from(long amount, HbarUnit unit) {
        try {
            return new Hbar(amount * unit.tinybar, unit);
        } catch (ArithmeticException e) {
            throw new HbarRangeException(amount + " " + unit + " is out of range for Hbar");
        }
    }

    /**
     * Calculate an hbar amount given a value and a unit to interpret
     * it as.
     * <p>
     * The equivalent amount in tinybar must be an integer and fit in a {@code long}
     * (64-bit signed integer) as that is required by the Hedera network.
     * <p>
     * E.g. 1.234 is a valid amount in {@link HbarUnit#Millibar}
     * but 1.2345 is not as that is 1234.5 tinybar.
     *
     * @param amount the amount in the given unit, may be fractional and/or negative.
     * @param unit   the unit to multiply the amount by.
     * @return the calculated hbar value.
     * @throws HbarRangeException if the tinybar equivalent is not an integer
     *                                  or does not fit in a {@code long}.
     */
    public static Hbar from(BigDecimal amount, HbarUnit unit) {
        BigInteger tinybar;

        if (unit == HbarUnit.Tinybar) {
            try {
                // longValueExact() does this operation internally
                tinybar = amount.toBigIntegerExact();
            } catch (ArithmeticException e) {
                throw new HbarRangeException("tinybar amount is not an integer: " + amount);
            }
        } else {
            BigDecimal tinybarDecimal = amount.multiply(new BigDecimal(unit.tinybar));

            try {
                tinybar = tinybarDecimal.toBigIntegerExact();
            } catch (ArithmeticException e) {
                throw new HbarRangeException("tinybar equivalent of " + amount + " "
                    + unit + " (" + tinybarDecimal + ") is not an integer");
            }
        }

        try {
            return new Hbar(tinybar.longValueExact(), unit);
        } catch (ArithmeticException e) {
            throw new HbarRangeException(amount + " " + unit + " is out of range for Hbar");
        }
    }

    /**
     * Wrap an amount of tinybar; not to be confused with {@link #of(long)}.
     *
     * @param amount the amount, in tinybar; may be negative.
     * @return the wrapped hbar value.
     */
    public static Hbar fromTinybar(long amount) {
        return new Hbar(amount, HbarUnit.Tinybar);
    }

    /**
     * @deprecated use {@code new Hbar()} to create a value of this in Hbar.
     */
    @Deprecated
    public static Hbar of(long amount) {
        return from(amount, HbarUnit.Hbar);
    }

    /**
     * @deprecated use {@code new Hbar()} to create a value of this in Hbar.
     */
    @Deprecated
    public static Hbar of(BigDecimal amount) {
        return from(amount, HbarUnit.Hbar);
    }

    /**
     * Convert the hbar value to a different unit; the result may be fractional.
     *
     * @param unit the unit to reinterpret the value as.
     * @return the reinterpreted value.
     */
    public BigDecimal as(HbarUnit unit) {
        if (unit == HbarUnit.Tinybar) {
            return new BigDecimal(tinybar);
        }

        return new BigDecimal(tinybar).divide(new BigDecimal(unit.tinybar), MathContext.UNLIMITED);
    }

    /**
     * Get the equivalent tinybar amount.
     */
    public long asTinybar() {
        return tinybar;
    }

    /**
     * Get a human-readable printout of this hbar value for debugging purposes.
     *
     * Not meant to be shown to users (localization/pretty-printing are not implemented).
     * The output format is unspecified.
     */
    public String toString() {
        // print with the original unit if we did a conversion, possibly more useful for debugging
        if (originalUnit != HbarUnit.Tinybar) {
            return as(originalUnit) + " " + originalUnit.getSymbol()
                + " (" + tinybar + " " + HbarUnit.Tinybar.getSymbol() + ")";
        }

         return tinybar + " " + HbarUnit.Tinybar.getSymbol();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Hbar hbar = (Hbar) o;

        return tinybar == hbar.tinybar;
    }

    @Override
    public int hashCode() {
        // generated by IntelliJ
        return Long.hashCode(tinybar);
    }

    @Override
    public int compareTo(Hbar o) {
        return Long.compare(tinybar, o.tinybar);
    }
}
