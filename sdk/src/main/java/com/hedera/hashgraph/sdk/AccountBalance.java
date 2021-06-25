package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.CryptoGetAccountBalanceResponse;
import com.hedera.hashgraph.sdk.proto.TokenBalance;

import java.util.Map;
import java.util.HashMap;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;

public class AccountBalance {
    @Nonnegative
    public final Hbar hbars;

    /**
     * @deprecated - Use `tokens` instead
     */
    @Deprecated
    @Nonnegative
    public final Map<TokenId, Long> token = new HashMap<>();

    public final Map<TokenId, Long> tokens;

    @Nonnegative
    public final Map<Long, Integer> tokenDecimals;

    AccountBalance(Hbar hbars, Map<TokenId, Long> token, Map<Long, Integer> decimal) {
        this.hbars = hbars;
        this.tokens = token;
        this.tokenDecimals = decimal;
    }

    static AccountBalance fromProtobuf(CryptoGetAccountBalanceResponse protobuf) {
        return AccountBalance.fromProtobuf(protobuf, null);
    }

    static AccountBalance fromProtobuf(CryptoGetAccountBalanceResponse protobuf, @Nullable NetworkName networkName) {
        var balanceList = protobuf.getTokenBalancesList();
        Map<TokenId, Long> map = new HashMap<>();
        Map<Long, Integer>  decimalMap = new HashMap<>();
        for (int i = 0; i < protobuf.getTokenBalancesCount(); i++) {
            map.put(TokenId.fromProtobuf(balanceList.get(i).getTokenId(), networkName), balanceList.get(i).getBalance());
            decimalMap.put(balanceList.get(i).getBalance(), balanceList.get(i).getDecimals());
        }

        return new AccountBalance(Hbar.fromTinybars(protobuf.getBalance()), map, decimalMap);
    }

    static AccountBalance fromBytes(byte[] data) throws InvalidProtocolBufferException {
        return fromProtobuf(CryptoGetAccountBalanceResponse.parseFrom(data));
    }

    CryptoGetAccountBalanceResponse toProtobuf() {
        var protobuf = CryptoGetAccountBalanceResponse.newBuilder()
            .setBalance(hbars.toTinybars());

        for (var entry : tokens.entrySet()) {
            protobuf.addTokenBalances(TokenBalance.newBuilder()
                .setTokenId(entry.getKey().toProtobuf())
                .setBalance(entry.getValue())
                .setDecimals(tokenDecimals.get(entry.getValue()))
            );
        }

        return protobuf.build();
    }

    ByteString toBytes() {
        return toProtobuf().toByteString();
    }
}
