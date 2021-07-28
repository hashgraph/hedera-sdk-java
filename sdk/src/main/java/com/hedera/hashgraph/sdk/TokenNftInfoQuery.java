package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.TokenGetNftInfoQuery;
import com.hedera.hashgraph.sdk.proto.TokenGetNftInfosQuery;
import com.hedera.hashgraph.sdk.proto.TokenGetAccountNftInfosQuery;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import io.grpc.MethodDescriptor;
import java8.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import javax.annotation.Nonnegative;
import javax.annotation.Nullable;

public class TokenNftInfoQuery extends com.hedera.hashgraph.sdk.Query<List<TokenNftInfo>, TokenNftInfoQuery> {
    @Nullable
    private TokenGetNftInfoQuery.Builder byNftBuilder = null;
    @Nullable
    private TokenGetNftInfosQuery.Builder byTokenBuilder = null;
    @Nullable
    private TokenGetAccountNftInfosQuery.Builder byAccountBuilder = null;
    @Nullable
    private NftId nftId = null;
    @Nullable
    private TokenId tokenId = null;
    @Nullable
    private AccountId accountId = null;
    private long start = 0;
    private long end = 0;

    public TokenNftInfoQuery() {
    }

    private boolean isByNft() {
        return byNftBuilder != null;
    }

    private boolean isByToken() {
        return byTokenBuilder != null;
    }

    private boolean isByAccount() {
        return byAccountBuilder != null;
    }

    /**
     * Sets the NFT ID for which information is requested.
     *
     * @return {@code this}
     * @param nftId The NftId to be set
     */
    public TokenNftInfoQuery byNftId(NftId nftId) {
        byNftBuilder = TokenGetNftInfoQuery.newBuilder()
            .setNftID(nftId.toProtobuf());
        this.nftId = nftId;
        return this;
    }

    @Nullable
    public NftId getNftId() {
        return nftId;
    }

    /**
     * Sets the Token ID and the index range for which information is requested.
     *
     * @return {@code this}
     * @param tokenId The ID of the token for which information is requested
     */
    public TokenNftInfoQuery byTokenId(TokenId tokenId) {
        byTokenBuilder = TokenGetNftInfosQuery
            .newBuilder()
            .setTokenID(Objects.requireNonNull(tokenId).toProtobuf());
        this.tokenId = tokenId;
        return this;
    }

    @Nullable
    public TokenId getTokenId() {
        return tokenId;
    }

    /**
     * Sets the Account ID for which information is requested.
     *
     * @return {@code this}
     * @param accountId The Account ID for which information is requested
     *
     */
    public TokenNftInfoQuery byAccountId(AccountId accountId) {
        byAccountBuilder = TokenGetAccountNftInfosQuery
            .newBuilder()
            .setAccountID(Objects.requireNonNull(accountId).toProtobuf());
        this.accountId = accountId;
        return this;
    }

    @Nullable
    public AccountId getAccountId() {
        return accountId;
    }

    /**
     * Sets the start of the index range for which information is requested.
     *
     * @return {@code this}
     * @param start The start index (inclusive) of the range of NFTs to query for. Value must be in the range [0; ownedNFTs-1]
     */
    public TokenNftInfoQuery setStart(@Nonnegative long start) {
        this.start = start;
        return this;
    }

    public long getStart() {
        return start;
    }

    /**
     * Sets the end of the index range for which information is requested.
     *
     * @return {@code this}
     * @param end The end index (exclusive) of the range of NFTs to query for. Value must be in the range (start; ownedNFTs]
     */
    public TokenNftInfoQuery setEnd(@Nonnegative long end) {
        this.end = end;
        return this;
    }

    public long getEnd() {
        return end;
    }

    @Override
    void validateChecksums(Client client) throws InvalidChecksumException {
        if(nftId != null) {
            nftId.tokenId.validateChecksum(client);
        }

        if(tokenId != null) {
            tokenId.validateChecksum(client);
        }

        if(accountId != null) {
            accountId.validateChecksum(client);
        }
    }

    @Override
    CompletableFuture<Void> onExecuteAsync(Client client) {
        int modesEnabled = (isByNft() ? 1 : 0) + (isByToken() ? 1 : 0) + (isByAccount() ? 1 : 0);
        if(modesEnabled > 1) {
            throw new IllegalStateException("TokenNftInfoQuery must be one of byNftId, byTokenId, or byAccountId, but multiple of these modes have been selected");
        } else if(modesEnabled == 0) {
            throw new IllegalStateException("TokenNftInfoQuery must be one of byNftId, byTokenId, or byAccountId, but none of these modes have been selected");
        }
        return super.onExecuteAsync(client);
    }

    @Override
    void onMakeRequest(com.hedera.hashgraph.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        if(isByNft()) {
            queryBuilder.setTokenGetNftInfo(Objects.requireNonNull(byNftBuilder).setHeader(header));
        } else if(isByToken()) {
            queryBuilder.setTokenGetNftInfos(Objects.requireNonNull(byTokenBuilder).setStart(start).setEnd(end).setHeader(header));
        } else /* is by account */ {
            queryBuilder.setTokenGetAccountNftInfos(Objects.requireNonNull(byAccountBuilder).setStart(start).setEnd(end).setHeader(header));
        }
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        if(isByNft()) {
            return response.getTokenGetNftInfo().getHeader();
        } else if(isByToken()) {
            return response.getTokenGetNftInfos().getHeader();
        } else /* is by account */ {
            return response.getTokenGetAccountNftInfos().getHeader();
        }
    }

    @Override
    QueryHeader mapRequestHeader(com.hedera.hashgraph.sdk.proto.Query request) {
        if(isByNft()) {
            return request.getTokenGetInfo().getHeader();
        } else if(isByToken()) {
            return request.getTokenGetNftInfos().getHeader();
        } else /* is by account */ {
            return request.getTokenGetAccountNftInfos().getHeader();
        }
    }

    private static List<TokenNftInfo> infosFromProtos(List<com.hedera.hashgraph.sdk.proto.TokenNftInfo> protoList) {
        var infos = new ArrayList<TokenNftInfo>();
        for(var proto : protoList) {
            infos.add(TokenNftInfo.fromProtobuf(proto));
        }
        return infos;
    }

    @Override
    List<TokenNftInfo> mapResponse(Response response, AccountId nodeId, com.hedera.hashgraph.sdk.proto.Query request) {
        if(isByNft()) {
            return Collections.singletonList(TokenNftInfo.fromProtobuf(response.getTokenGetNftInfo().getNft()));
        } else if(isByToken()) {
            return infosFromProtos(response.getTokenGetNftInfos().getNftsList());
        } else /* is by account */ {
            return infosFromProtos(response.getTokenGetAccountNftInfos().getNftsList());
        }
    }

    @Override
    MethodDescriptor<Query, Response> getMethodDescriptor() {
        if(isByNft()) {
            return TokenServiceGrpc.getGetTokenNftInfoMethod();
        } else if(isByToken()) {
            return TokenServiceGrpc.getGetTokenNftInfosMethod();
        } else /* is by account */ {
            return TokenServiceGrpc.getGetAccountNftInfosMethod();
        }
    }

    @Override
    public CompletableFuture<Hbar> getCostAsync(Client client) {
        // deleted accounts return a COST_ANSWER of zero which triggers `INSUFFICIENT_TX_FEE`
        // if you set that as the query payment; 25 tinybar seems to be enough to get
        // `Token_DELETED` back instead.
        return super.getCostAsync(client).thenApply((cost) -> Hbar.fromTinybars(Math.max(cost.toTinybars(), 25)));
    }
}
