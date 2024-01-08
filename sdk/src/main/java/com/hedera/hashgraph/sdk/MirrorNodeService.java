package com.hedera.hashgraph.sdk;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.proto.TokenBalance;
import com.hedera.hashgraph.sdk.proto.TokenFreezeStatus;
import com.hedera.hashgraph.sdk.proto.TokenKycStatus;
import com.hedera.hashgraph.sdk.proto.TokenRelationship;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

class MirrorNodeService {

    private final MirrorNodeGateway mirrorNodeGateway;

    MirrorNodeService(MirrorNodeGateway mirrorNodeGateway) {
        this.mirrorNodeGateway = mirrorNodeGateway;
    }

    List<TokenBalance> getTokenBalancesForAccount(String id) {
        JsonObject accountTokensResponse;

        try {
            accountTokensResponse = mirrorNodeGateway.getAccountTokens(id);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error, while processing getAccountInfo mirror node query", e);
        }
        JsonArray tokens = accountTokensResponse.get("tokens").getAsJsonArray();

        List<TokenBalance> tokenBalanceList = tokens.asList().stream().map(jsonElement -> {
            var jsonObject = jsonElement.getAsJsonObject();
            var tokenId = jsonObject.get("token_id").getAsString();
            var tokenBalance = jsonObject.get("balance").getAsLong();

            return TokenBalance.newBuilder().setTokenId(TokenId.fromString(tokenId).toProtobuf())
                .setBalance(tokenBalance)
                // no tokenDecimals :( -- additional query per each token is needed to figure it out
//                        .setDecimals()
                .build();
        }).collect(Collectors.toList());

        return tokenBalanceList;
    }

    List<TokenRelationship> getTokenRelationshipsForAccount(String id) {
        JsonObject accountTokensResponse;
        try {
            accountTokensResponse = mirrorNodeGateway.getAccountTokens(id);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error, while processing getAccountTokens mirror node query", e);
        }

        JsonArray tokens = accountTokensResponse.get("tokens").getAsJsonArray();

        List<TokenRelationship> tokenRelationships = tokens.asList().stream().map(jsonElement -> {
            var jsonObject = jsonElement.getAsJsonObject();
            var tokenId = jsonObject.get("token_id").getAsString();
            var balance = jsonObject.get("balance").getAsLong();
            var kycStatus = jsonObject.get("kyc_status").getAsString();
            var freezeStatus = jsonObject.get("freeze_status").getAsString();
            var automaticAssociation = jsonObject.get("automatic_association").getAsBoolean();

            return com.hedera.hashgraph.sdk.proto.TokenRelationship.newBuilder()
                .setTokenId(TokenId.fromString(tokenId).toProtobuf())
                // no symbol :( -- additional query per each token is needed to figure it out
//                .setSymbol()
                .setBalance(balance)
                .setKycStatus(getTokenKycStatusFromString(kycStatus))
                .setFreezeStatus(getTokenFreezeStatusFromString(freezeStatus))
                .setAutomaticAssociation(automaticAssociation)
                .build();
        }).collect(Collectors.toList());

        return tokenRelationships;
    }

    private TokenKycStatus getTokenKycStatusFromString(String tokenKycStatusString) {
        return switch (tokenKycStatusString) {
            case "NOT_APPLICABLE" -> TokenKycStatus.KycNotApplicable;
            case "GRANTED" -> TokenKycStatus.Granted;
            case "REVOKED" -> TokenKycStatus.Revoked;
            default -> throw new IllegalArgumentException("Invalid token KYC status: " + tokenKycStatusString);
        };
    }

    private TokenFreezeStatus getTokenFreezeStatusFromString(String tokenFreezeStatusString) {
        return switch (tokenFreezeStatusString) {
            case "NOT_APPLICABLE" -> TokenFreezeStatus.FreezeNotApplicable;
            case "FROZEN" -> TokenFreezeStatus.Frozen;
            case "UNFROZEN" -> TokenFreezeStatus.Unfrozen;
            default -> throw new IllegalArgumentException("Invalid token freeze status: " + tokenFreezeStatusString);
        };
    }
}
