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
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import javax.annotation.Nonnegative;
import javax.annotation.Nullable;

public class TokenNftInfoQuery extends com.hedera.hashgraph.sdk.Query<List<TokenNftInfo>, TokenNftInfoQuery> {
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

    /**
     * Sets the NFT ID for which information is requested.
     *
     * @return {@code this}
     * @param nftId The NftId to be set
     */
    public TokenNftInfoQuery byNftId(NftId nftId) {
        Objects.requireNonNull(nftId);
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
        Objects.requireNonNull(tokenId);
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
        Objects.requireNonNull(accountId);
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
    void validateChecksums(Client client) throws BadEntityIdException {
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
        int modesEnabled = (nftId != null ? 1 : 0) + (tokenId != null ? 1 : 0) + (accountId != null ? 1 : 0);
        if(modesEnabled > 1) {
            throw new IllegalStateException("TokenNftInfoQuery must be one of byNftId, byTokenId, or byAccountId, but multiple of these modes have been selected");
        } else if(modesEnabled == 0) {
            throw new IllegalStateException("TokenNftInfoQuery must be one of byNftId, byTokenId, or byAccountId, but none of these modes have been selected");
        }
        return super.onExecuteAsync(client);
    }

    @Override
    void onMakeRequest(com.hedera.hashgraph.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        if(nftId != null) {
            queryBuilder.setTokenGetNftInfo(
                TokenGetNftInfoQuery.newBuilder()
                    .setNftID(nftId.toProtobuf())
                    .setHeader(header)
            );
        } else if(tokenId != null) {
            queryBuilder.setTokenGetNftInfos(
                TokenGetNftInfosQuery.newBuilder()
                    .setTokenID(tokenId.toProtobuf())
                    .setStart(start)
                    .setEnd(end)
                    .setHeader(header)
            );
        } else /* is by account */ {
            queryBuilder.setTokenGetAccountNftInfos(
                TokenGetAccountNftInfosQuery.newBuilder()
                    .setAccountID(accountId.toProtobuf())
                    .setStart(start)
                    .setEnd(end)
                    .setHeader(header)
            );
        }
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        if(nftId != null) {
            return response.getTokenGetNftInfo().getHeader();
        } else if(tokenId != null) {
            return response.getTokenGetNftInfos().getHeader();
        } else /* is by account */ {
            return response.getTokenGetAccountNftInfos().getHeader();
        }
    }

    @Override
    QueryHeader mapRequestHeader(com.hedera.hashgraph.sdk.proto.Query request) {
        if(nftId != null) {
            return request.getTokenGetInfo().getHeader();
        } else if(tokenId != null) {
            return request.getTokenGetNftInfos().getHeader();
        } else /* is by account */ {
            return request.getTokenGetAccountNftInfos().getHeader();
        }
    }

    private static List<TokenNftInfo> infosFromProtos(List<com.hedera.hashgraph.sdk.proto.TokenNftInfo> protoList) {
        var infoList = new ArrayList<TokenNftInfo>();
        for(var proto : protoList) {
            infoList.add(TokenNftInfo.fromProtobuf(proto));
        }
        return infoList;
    }

    @Override
    List<TokenNftInfo> mapResponse(Response response, AccountId nodeId, com.hedera.hashgraph.sdk.proto.Query request) {
        if(nftId != null) {
            return Collections.singletonList(TokenNftInfo.fromProtobuf(response.getTokenGetNftInfo().getNft()));
        } else if(tokenId != null) {
            return infosFromProtos(response.getTokenGetNftInfos().getNftsList());
        } else /* is by account */ {
            return infosFromProtos(response.getTokenGetAccountNftInfos().getNftsList());
        }
    }

    @Override
    MethodDescriptor<Query, Response> getMethodDescriptor() {
        if(nftId != null) {
            return TokenServiceGrpc.getGetTokenNftInfoMethod();
        } else if(tokenId != null) {
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
