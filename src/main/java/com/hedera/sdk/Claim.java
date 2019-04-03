package com.hedera.sdk;

import com.hedera.sdk.crypto.Key;
import com.hedera.sdk.proto.AccountID;
import java.util.List;
import java.util.stream.Collectors;

public final class Claim implements Entity {
    private final com.hedera.sdk.proto.Claim inner;

    public Claim(com.hedera.sdk.proto.Claim inner) {
        this.inner = inner;
    }

    public AccountId getAcccount() {
        AccountID account = this.inner.getAccountID();

        return new AccountId(account.getShardNum(), account.getRealmNum(), account.getAccountNum());
    }

    public byte[] getHash() {
        return this.inner.getHash()
            .toByteArray();
    }

    public List<Key> getKeys() {
        return this.inner.getKeys()
            .getKeysList()
            .stream()
            .map(Key::fromProtoKey)
            .collect(Collectors.toList());
    }
}
