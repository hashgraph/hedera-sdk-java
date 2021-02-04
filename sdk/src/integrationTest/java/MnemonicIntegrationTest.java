import com.hedera.hashgraph.sdk.*;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Assert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MnemonicIntegrationTest {
    private static final String MNEMONIC3_STRING = "obvious favorite remain caution remove laptop base vacant increase video erase pass sniff sausage knock grid argue salt romance way alone fever slush dune";
    private static final String MNEMONIC3_KEY = "302e020100300506032b6570042204202b7345f302a10c2a6d55bf8b7af40f125ec41d780957826006d30776f0c441fb";

    private static final String MNEMONIC_LEGACY_STRING = "jolly kidnap tom lawn drunk chick optic lust mutter mole bride galley dense member sage neural widow decide curb aboard margin manure";
    private static final String MNEMONIC_LEGACY_PRIVATE_KEY = "302e020100300506032b657004220420882a565ad8cb45643892b5366c1ee1c1ef4a730c5ce821a219ff49b6bf173ddf";

    private static final String MNEMONIC_STRING = "inmate flip alley wear offer often piece magnet surge toddler submit right radio absent pear floor belt raven price stove replace reduce plate home";
    private static final String MNEMONIC_PRIVATE_KEY = "302e020100300506032b657004220420853f15aecd22706b105da1d709b4ac05b4906170c2b9c7495dff9af49e1391da";

    @Test
    @DisplayName("Mnemonic 3 test")
    void thirdMnemonicTest() {
        assertDoesNotThrow(() -> {
            Mnemonic mnemonic = assertDoesNotThrow(() -> Mnemonic.fromString(MNEMONIC3_STRING));
            PrivateKey key = mnemonic.toLegacyPrivateKey();
            assertEquals(key.toString(), MNEMONIC3_KEY);
        });
    }

    @Test
    @DisplayName("Legacy mnemonic test")
    void legacyMnemonicTest() {
        assertDoesNotThrow(() -> {
            Mnemonic mnemonic = assertDoesNotThrow(() -> Mnemonic.fromString(MNEMONIC_LEGACY_STRING));
            PrivateKey key = mnemonic.toLegacyPrivateKey();
            assertEquals(key.toString(), MNEMONIC_LEGACY_PRIVATE_KEY);
        });
    }

    @Test
    @DisplayName("Mnemonic test")
    void mnemonicTest() {
        assertDoesNotThrow(() -> {
            Mnemonic mnemonic = assertDoesNotThrow(() -> Mnemonic.fromString(MNEMONIC_STRING));
            PrivateKey key = mnemonic.toPrivateKey();
            assertEquals(key.toString(), MNEMONIC_PRIVATE_KEY);
        });
    }
}
