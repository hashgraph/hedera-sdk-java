/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.CryptoGetAccountBalanceResponse;
import com.hedera.hashgraph.sdk.proto.TokenBalance;

import javax.annotation.Nonnegative;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This class represents the account balance object
 */
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
    public final Map<TokenId, Integer> tokenDecimals;

    AccountBalance(Hbar hbars, Map<TokenId, Long> token, Map<TokenId, Integer> decimal) {
        this.hbars = hbars;
        this.tokens = token;
        this.tokenDecimals = decimal;
    }

    /**
     * Convert the protobuf object to an account balance object.
     *
     * @param protobuf                  protobuf response object
     * @return                          the converted account balance object
     */
    static AccountBalance fromProtobuf(CryptoGetAccountBalanceResponse protobuf) {
        var balanceList = protobuf.getTokenBalancesList();
        Map<TokenId, Long> map = new HashMap<>();
        Map<TokenId, Integer> decimalMap = new HashMap<>();
        for (int i = 0; i < protobuf.getTokenBalancesCount(); i++) {
            map.put(TokenId.fromProtobuf(balanceList.get(i).getTokenId()), balanceList.get(i).getBalance());
            decimalMap.put(TokenId.fromProtobuf(balanceList.get(i).getTokenId()), balanceList.get(i).getDecimals());
        }

        return new AccountBalance(Hbar.fromTinybars(protobuf.getBalance()), map, decimalMap);
    }

    /**
     * Convert a byte array to an account balance object.
     *
     * @param data                      the byte array
     * @return                          the converted account balance object
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    public static AccountBalance fromBytes(byte[] data) throws InvalidProtocolBufferException {
        return fromProtobuf(CryptoGetAccountBalanceResponse.parseFrom(data));
    }

    /**
     * Convert an account balance object into a protobuf.
     *
     * @return                          the protobuf object
     */
    CryptoGetAccountBalanceResponse toProtobuf() {
        var protobuf = CryptoGetAccountBalanceResponse.newBuilder()
            .setBalance(hbars.toTinybars());

        for (var entry : tokens.entrySet()) {
            protobuf.addTokenBalances(TokenBalance.newBuilder()
                .setTokenId(entry.getKey().toProtobuf())
                .setBalance(entry.getValue())
                .setDecimals(Objects.requireNonNull(tokenDecimals.get(entry.getKey())))
            );
        }

        return protobuf.build();
    }

    /**
     * Convert the account balance object to a byte array.
     *
     * @return                          the converted account balance object
     */
    public ByteString toBytes() {
        return toProtobuf().toByteString();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("hbars", hbars)
            .add("tokens", tokens)
            .toString();
    }
}
