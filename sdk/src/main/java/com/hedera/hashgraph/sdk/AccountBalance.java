package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.CryptoGetAccountBalanceResponse;
import com.hedera.hashgraph.sdk.proto.TokenBalance;

import java.util.Map;
import java.util.HashMap;

import javax.annotation.Nonnegative;

public class AccountBalance {
    @Nonnegative
    public final Hbar hbars;

    @Nonnegative
    public final Map<TokenId, Long> token;

    AccountBalance(Hbar hbars, Map<TokenId, Long> token) {
        this.hbars = hbars;
        this.token = token;
    }

    static AccountBalance fromProtobuf(CryptoGetAccountBalanceResponse protobuf) {
        var balanceList = protobuf.getTokenBalancesList();
        Map<TokenId, Long> map = new HashMap<>();
        for (int i = 0; i < protobuf.getTokenBalancesCount(); i++) {
            map.put(TokenId.fromProtobuf(balanceList.get(i).getTokenId()), balanceList.get(i).getBalance());
        }

        return new AccountBalance(Hbar.fromTinybars(protobuf.getBalance()), map);
    }

    static AccountBalance fromBytes(byte[] data) throws InvalidProtocolBufferException {
        return fromProtobuf(CryptoGetAccountBalanceResponse.parseFrom(data));
    }

    CryptoGetAccountBalanceResponse toProtobuf() {
        var protobuf = CryptoGetAccountBalanceResponse.newBuilder()
            .setBalance(hbars.toTinybars());

        for (var entry : token.entrySet()) {
            protobuf.addTokenBalances(TokenBalance.newBuilder()
                .setBalance(entry.getValue())
                .setTokenId(entry.getKey().toProtobuf())
            );
        }

        return protobuf.build();
    }

    ByteString toBytes() {
        return toProtobuf().toByteString();
    }
}
