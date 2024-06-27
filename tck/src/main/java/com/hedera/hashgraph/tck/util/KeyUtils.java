package com.hedera.hashgraph.tck.util;

import com.hedera.hashgraph.sdk.Key;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;

public final class KeyUtils {

    // TODO: cover with test(s)
    public static Key getKeyFromStringDER(String keyStringDER) {
        try {
            return PublicKey.fromStringDER(keyStringDER);
        } catch (Exception e) {
            return PrivateKey.fromStringDER(keyStringDER);
        }
    }
}
