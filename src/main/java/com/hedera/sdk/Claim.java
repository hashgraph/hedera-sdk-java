package com.hedera.sdk;

import com.hedera.sdk.crypto.Key;
import com.hedera.sdk.proto.AccountID;
import java.util.ArrayList;
import java.util.List;

public final class Claim {
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
        return this.innner.getHash().toByteArray();
    }

    public List<Key> getKeys() throws Exception {
        List<com.hedera.sdk.proto.Key> protoKeys = this.innner.getKeys().getKeysList();
        ArrayList<Key> keys = new ArrayList<Key>();

        for (com.hedera.sdk.proto.Key key : protoKeys) {
            keys.add(Key.fromProtoKey(key));
        }

        return keys;
    }
}
