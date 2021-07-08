package com.hedera.hashgraph.sdk.token;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.hedera.hashgraph.sdk.Internal;
import com.hedera.hashgraph.sdk.account.AccountId;

@Beta
public class TokenNftTransfer {
    public final AccountId sender;
    public final AccountId receiver;
    public final long serial;

    public TokenNftTransfer(AccountId sender, AccountId receiver, long serial) {
        this.sender = sender;
        this.receiver = receiver;
        this.serial = serial;
    }

    @Internal
    public TokenNftTransfer(com.hedera.hashgraph.proto.NftTransfer nftTransfer) {
        this.sender = new AccountId(nftTransfer.getSenderAccountID());
        this.receiver = new AccountId(nftTransfer.getReceiverAccountID());
        this.serial = nftTransfer.getSerialNumber();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("sender", sender)
            .add("receiver", receiver)
            .add("serial", serial)
            .toString();
    }
}
