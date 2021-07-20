package com.hedera.hashgraph.sdk;

import java8.util.stream.RefStreams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HbarTest {
    private final long fiftyGTinybar = 5_000_000_000L;
    private final Hbar fiftyHbar = Hbar.fromTinybars(fiftyGTinybar);

    private final Hbar hundredHbar = new Hbar(100);

    private final Hbar negativeFiftyHbar = new Hbar(-50);

    static Iterator<Arguments> getValueConversions() {
        return RefStreams.of(
            Arguments.arguments(new BigDecimal(50_000_000), HbarUnit.MICROBAR),
            Arguments.arguments(new BigDecimal(50_000), HbarUnit.MILLIBAR),
            Arguments.arguments(new BigDecimal(50), HbarUnit.HBAR),
            Arguments.arguments(new BigDecimal("0.05"), HbarUnit.KILOBAR),
            Arguments.arguments(new BigDecimal("0.00005"), HbarUnit.MEGABAR),
            Arguments.arguments(new BigDecimal("0.00000005"), HbarUnit.GIGABAR)
        ).iterator();
    }

    @Test
    void shouldConstruct() {
        assertEquals(fiftyHbar.toTinybars(), fiftyGTinybar);
        assertEquals(fiftyHbar.to(HbarUnit.HBAR), new BigDecimal(50));
        assertEquals(new Hbar(50).toTinybars(), fiftyGTinybar);
        assertEquals(Hbar.fromTinybars(fiftyGTinybar).toTinybars(), fiftyGTinybar);
    }

    @Test
    void shouldNotConstruct() {
        assertThrows(Exception.class, () -> new Hbar(new BigDecimal("0.1"), HbarUnit.TINYBAR));
    }

    @Test
    void shouldDisplay() {
        assertEquals("50 ℏ", fiftyHbar.toString());
        assertEquals("-50 ℏ", negativeFiftyHbar.toString());
        assertEquals("1 tℏ", Hbar.fromTinybars(1).toString());
        assertEquals("-1 tℏ", Hbar.fromTinybars(1).negated().toString());
        assertEquals("1000 tℏ", Hbar.fromTinybars(1000).toString());
        assertEquals("-1000 tℏ", Hbar.fromTinybars(1000).negated().toString());
    }

    @ParameterizedTest
    @MethodSource("getValueConversions")
    void shouldConvert(BigDecimal value, HbarUnit unit) {
        assertEquals(Hbar.from(value, unit), fiftyHbar);
        assertEquals(fiftyHbar.to(unit), value);
    }

    @Test
    void shouldCompare() {
        assertEquals(fiftyHbar, fiftyHbar);
        assertNotEquals(fiftyHbar, hundredHbar);

        assertEquals(fiftyHbar.compareTo(new Hbar(50)), 0);

        assertTrue(fiftyHbar.compareTo(hundredHbar) < 0);
        assertTrue(hundredHbar.compareTo(fiftyHbar) > 0);

        assertTrue(fiftyHbar.compareTo(negativeFiftyHbar) > 0);
    }

    @Test
    void constructorWorks() {
        new Hbar(1);
    }

    @Test
    void fromString() {
        assertEquals(Hbar.fromString("1").toTinybars(), 100_000_000);
        assertEquals(Hbar.fromString("ℏ 1").toTinybars(), 100_000_000);
        assertEquals(Hbar.fromString("mℏ1").toTinybars(), 100_000);
    }

    @Test
    void fromStringUnit() {
        assertEquals(Hbar.fromString("1", HbarUnit.TINYBAR).toTinybars(), 1);
    }

    @Test
    void from() {
        assertEquals(Hbar.from(1).toTinybars(), 100000000);
    }

    @Test
    void fromUnit() {
        assertEquals(Hbar.from(1, HbarUnit.TINYBAR).toTinybars(), 1);
    }

    @Test
    void getValue() {
        assertEquals(new Hbar(1).getValue(), BigDecimal.valueOf(1));
    }

    @Test
    void hasHashCode() {
        assertEquals(new Hbar(1).hashCode(), 100000031);
    }
}
