package com.hedera.hashgraph.sdk;

import java8.util.stream.RefStreams;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HbarTest {
    private final long fiftyGTinybar = 5_000_000_000L;
    private final Hbar fiftyHbar = Hbar.fromTinybar(fiftyGTinybar);

    private final Hbar hundredHbar = new Hbar(100);

    private final Hbar negativeFiftyHbar = new Hbar(-50);

    static Iterator<Arguments> getValueConversions() {
        return RefStreams.of(
            Arguments.arguments(new BigDecimal(50_000_000), HbarUnit.Microbar),
            Arguments.arguments(new BigDecimal(50_000), HbarUnit.Millibar),
            Arguments.arguments(new BigDecimal(50), HbarUnit.Hbar),
            Arguments.arguments(new BigDecimal("0.05"), HbarUnit.Kilobar),
            Arguments.arguments(new BigDecimal("0.00005"), HbarUnit.Megabar),
            Arguments.arguments(new BigDecimal("0.00000005"), HbarUnit.Gigabar)
        ).iterator();
    }

    @Test
    @DisplayName("factory method checks")
    void factoryMethods() {
        assertEquals(fiftyHbar.asTinybar(), fiftyGTinybar);
        assertEquals(fiftyHbar.as(HbarUnit.Hbar), new BigDecimal(50));

        assertEquals(new Hbar(50).asTinybar(), fiftyGTinybar);
        assertEquals(Hbar.fromTinybar(fiftyGTinybar).asTinybar(), fiftyGTinybar);
        assertEquals(Hbar.ZERO.asTinybar(), 0);
    }

    @ParameterizedTest
    @MethodSource("getValueConversions")
    @DisplayName("value conversions work correctly")
    void valueConversions(BigDecimal value, HbarUnit unit) {
        assertEquals(Hbar.from(value, unit), fiftyHbar);
        assertEquals(fiftyHbar.as(unit), value);
    }

    @Test
    @DisplayName("comparison works correctly")
    void comparisons() {
        assertEquals(fiftyHbar, fiftyHbar);
        assertNotEquals(fiftyHbar, hundredHbar);
        assertNotEquals(fiftyHbar, Hbar.ZERO);

        assertEquals(fiftyHbar.compareTo(new Hbar(50)), 0);

        assertTrue(fiftyHbar.compareTo(Hbar.MIN) > 0);
        assertTrue(fiftyHbar.compareTo(Hbar.ZERO) > 0);
        assertTrue(fiftyHbar.compareTo(Hbar.MAX) < 0);

        assertTrue(fiftyHbar.compareTo(hundredHbar) < 0);
        assertTrue(hundredHbar.compareTo(fiftyHbar) > 0);

        assertTrue(fiftyHbar.compareTo(negativeFiftyHbar) > 0);
    }
}
