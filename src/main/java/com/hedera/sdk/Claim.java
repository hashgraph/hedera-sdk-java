package com.hedera.sdk;

import com.hedera.sdk.crypto.Key;
import com.hedera.sdk.proto.AccountID;
import java.util.List;
import java.util.stream.Collectors;

public final class Claim implements Entity {
    private final com.hedera.sdk.proto.Claim innner;

    public Claim(com.hedera.sdk.proto.Claim inner) {
        this.innner = inner;
    }

    public AccountId getAcccount() {
        AccountID account = this.innner.getAccountID();

        return new AccountId(account.getShardNum(), account.getRealmNum(), account.getAccountNum());
    }

    public byte[] getHash() {
        return this.innner.getHash()
            .toByteArray();
    }

    public List<Key> getKeys() {
        return this.innner.getKeys()
            .getKeysList()
            .stream()
            .map(Key::fromProtoKey)
            .collect(Collectors.toList());
    }
}
