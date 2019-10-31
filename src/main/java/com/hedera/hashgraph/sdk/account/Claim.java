package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.sdk.Entity;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import com.hederahashgraph.api.proto.java.AccountID;

import java.util.List;
import java.util.stream.Collectors;

public final class Claim implements Entity {
    private final com.hederahashgraph.api.proto.java.Claim inner;

    public Claim(com.hederahashgraph.api.proto.java.Claim inner) {
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

    public List<PublicKey> getKeys() {
        return this.inner.getKeys()
            .getKeysList()
            .stream()
            .map(PublicKey::fromProtoKey)
            .collect(Collectors.toList());
    }
}
