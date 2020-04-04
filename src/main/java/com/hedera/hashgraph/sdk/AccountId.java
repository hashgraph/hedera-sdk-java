package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.AccountID;
import javax.annotation.Nonnegative;

public final class AccountId extends EntityId {
    public AccountId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num) {
        super(shard, realm, num);
    }

    static AccountId fromProtobuf(AccountID accountId) {
        return new AccountId(
                accountId.getShardNum(), accountId.getRealmNum(), accountId.getAccountNum());
    }

    AccountID toProtobuf() {
        return AccountID.newBuilder()
                .setShardNum(shard)
                .setRealmNum(realm)
                .setAccountNum(num)
                .build();
    }
}
