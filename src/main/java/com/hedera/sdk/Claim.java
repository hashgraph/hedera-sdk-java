package com.hedera.sdk;

import com.hedera.sdk.crypto.Key;
import com.hedera.sdk.proto.AccountID;
import java.util.List;
import java.util.stream.Collectors;

public final class Claim implements Entity {
    private final com.hedera.sdk.proto.Claim innner;

    private Claim(com.hedera.sdk.proto.Claim inner) {
        this.innner = inner;
    }

    public Claim fromProto(com.hedera.sdk.proto.Claim claim) {
        return new Claim(claim);
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
            .map(key -> Key.fromProtoKey(key))
            .collect(Collectors.toList());
    }
}
