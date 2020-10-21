package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.TokenBalances;

import java.util.Map;
import java.util.HashMap;

import javax.annotation.Nonnegative;

public class AccountBalance {
    @Nonnegative
    public final Hbar hbars;

    @Nonnegative
    public final Map<TokenId, Long> token;

    AccountBalance(Hbar hbars,Map<TokenId, Long> token) {
        this.hbars = hbars;
        this.token = token;
    }

//    static com.hedera.hashgraph.sdk.AccountBalance fromProtobuf(TokenBalances balance) {
//        var balanceList = balance.getTokenBalancesList();
//        Map<TokenId, Long> map = new HashMap<>();
//        for(int i = 0; i < balance.getTokenBalancesCount(); i++){
//            map.put(TokenId.fromProtobuf(balanceList.get(i).getTokenId()), balanceList.get(i).getBalance());
//        }
//            return new com.hedera.hashgraph.sdk.AccountBalance(
//                Hbar.fromTinybars(balance.getb), new Map<balance.);
//        }
}
