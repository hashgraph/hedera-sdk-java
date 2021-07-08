package com.hedera.hashgraph.sdk.token;

import com.google.common.annotations.Beta;
import com.hedera.hashgraph.proto.Query;
import com.hedera.hashgraph.proto.QueryHeader;
import com.hedera.hashgraph.proto.TokenGetAccountNftInfosQuery;
import com.hedera.hashgraph.proto.TokenGetNftInfoQuery;
import com.hedera.hashgraph.proto.TokenGetNftInfosQuery;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.proto.TokenServiceGrpc;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.HederaThrowable;
import com.hedera.hashgraph.sdk.QueryBuilder;
import com.hedera.hashgraph.sdk.account.AccountId;
import io.grpc.MethodDescriptor;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nonnegative;

@Beta
public class TokenNftInfoQuery extends QueryBuilder<List<TokenNftInfo>, TokenNftInfoQuery> {
    private TokenGetNftInfoQuery.Builder byNftBuilder;
    private TokenGetNftInfosQuery.Builder byTokenBuilder;
    private TokenGetAccountNftInfosQuery.Builder byAccountBuilder;

    public TokenNftInfoQuery() {
        super();

        byNftBuilder = inner.getTokenGetNftInfoBuilder();
        byTokenBuilder = inner.getTokenGetNftInfosBuilder();
        byAccountBuilder = inner.getTokenGetAccountNftInfosBuilder();
    }

    private boolean isByNft() {
        return byNftBuilder.hasNftID();
    }

    private boolean isByToken() {
        return byTokenBuilder.hasTokenID();
    }

    private boolean isByAccount() {
        return byAccountBuilder.hasAccountID();
    }

    /**
     * Sets the NFT ID for which information is requested.
     *
     * @return {@code this}
     * @param nftId The NftId to be set
     */
    public TokenNftInfoQuery byNftId(NftId nftId) {
        byNftBuilder = TokenGetNftInfoQuery.newBuilder()
            .setNftID(nftId.toProto());
        return this;
    }

    /**
     * Sets the Token ID and the index range for which information is requested.
     *
     * @return {@code this}
     * @param tokenId The ID of the token for which information is requested
     */
    public TokenNftInfoQuery byTokenId(TokenId tokenId, @Nonnegative long start, @Nonnegative long end) {
        byTokenBuilder.setTokenID(Objects.requireNonNull(tokenId).toProto()).setStart(start).setEnd(end);
        return this;
    }

    /**
     * Sets the Account ID for which information is requested.
     *
     * @return {@code this}
     * @param accountId The Account ID for which information is requested
     *
     */
    public TokenNftInfoQuery byAccountId(AccountId accountId, @Nonnegative long start, @Nonnegative long end) {
        byAccountBuilder.setAccountID(Objects.requireNonNull(accountId).toProto()).setStart(start).setEnd(end);
        return this;
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        if(isByNft()) {
            return byNftBuilder.getHeaderBuilder();
        } else if(isByToken()) {
            return byTokenBuilder.getHeaderBuilder();
        } else /* is by account */ {
            return byAccountBuilder.getHeaderBuilder();
        }    }

    @Override
    protected void doValidate() {
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        if(isByNft()) {
            return TokenServiceGrpc.getGetTokenNftInfoMethod();
        } else if(isByToken()) {
            return TokenServiceGrpc.getGetTokenNftInfosMethod();
        } else /* is by account */ {
            return TokenServiceGrpc.getGetAccountNftInfosMethod();
        }
    }

    @Override
    protected List<TokenNftInfo> extractResponse(Response raw) {
        if(isByNft()) {
            return Collections.singletonList(new TokenNftInfo(raw.getTokenGetNftInfo().getNft()));
        } else if(isByToken()) {
            return infosFromProtos(raw.getTokenGetNftInfos().getNftsList());
        } else /* is by account */ {
            return infosFromProtos(raw.getTokenGetAccountNftInfos().getNftsList());
        }
    }

    @Override
    public long getCost(Client client) throws HederaStatusException, HederaNetworkException {
        // deleted accounts return a COST_ANSWER of zero which triggers `INSUFFICIENT_TX_FEE`
        // if you set that as the query payment; 25 tinybar seems to be enough to get
        // `ACCOUNT_DELETED` back instead.
        return Math.max(super.getCost(client), 25);
    }

    @Override
    public void getCostAsync(Client client, Consumer<Long> withCost, Consumer<HederaThrowable> onError) {
        // see above
        super.getCostAsync(client, (cost) -> withCost.accept(Math.min(cost, 25)), onError);
    }

    private List<TokenNftInfo> infosFromProtos(List<com.hedera.hashgraph.proto.TokenNftInfo> protoList) {
        ArrayList<TokenNftInfo> infos = new ArrayList<>();
        for(com.hedera.hashgraph.proto.TokenNftInfo proto : protoList) {
            infos.add(new TokenNftInfo(proto));
        }
        return infos;
    }
}
