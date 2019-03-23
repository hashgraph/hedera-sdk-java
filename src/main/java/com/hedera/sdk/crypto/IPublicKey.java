package com.hedera.sdk.crypto;

import com.hedera.sdk.proto.Key;

public interface IPublicKey {
    Key toProtoKey();
}
