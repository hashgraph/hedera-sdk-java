package test.hedera.sdk.cryptography;

import static org.junit.Assert.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.hedera.sdk.cryptography.Reference;
import com.hedera.sdk.cryptography.Seed;

import java.util.List;
import java.util.Arrays;

@TestInstance(Lifecycle.PER_CLASS)
class SeedTest {
    
    @BeforeAll
    void setup() {}
    
    @Test
    void testFromWordList() {

        String ed25519RecoveryWords = "embark port duly poetry front verity quote lake toast Austin pig far pink clergy allied still injury night canine frenzy gamma heel";
        List<String> allWords = Reference.wordIndices(ed25519RecoveryWords);
        Seed seed = Seed.fromWordList(allWords);
        byte[] expectedSeedInBytes = new byte[]{-49, -125, 28, -53, -125, -9, -47, -42, -96, 38, 30, 42, 111, 105, 85, 45, -67, 69, 45, 123, 58, -113, -76, -11, -7, 96, -24, -86, -4, -16, -45, 47};
        assertEquals(Arrays.toString(expectedSeedInBytes), Arrays.toString(seed.toBytes()));
    }
}