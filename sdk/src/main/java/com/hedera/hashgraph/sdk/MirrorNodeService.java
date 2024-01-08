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

    /**
     * Get AccountId num from mirror node using evm address.
     *
     * @param evmAddress
     * @return
     */
    Long getAccountNum(String evmAddress) {
        JsonObject accountInfoResponse;

        try {
            accountInfoResponse = mirrorNodeGateway.getAccountInfo(evmAddress);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error, while processing getAccountInfo mirror node query", e);
        }

        String num = accountInfoResponse.get("account").getAsString();

        return Long.parseLong(num.substring(num.lastIndexOf(".") + 1));
    }

    /**
     * Get EvmAddress from mirror node using account num.
     *
     * @param num
     * @return
     */
    EvmAddress getAccountEvmAddress(long num) {
        JsonObject accountInfoResponse;

        try {
            accountInfoResponse = mirrorNodeGateway.getAccountInfo(String.valueOf(num));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error, while processing getAccountInfo mirror node query", e);
        }

        String evmAddressString = accountInfoResponse.get("evm_address").getAsString();

        return EvmAddress.fromString(
            evmAddressString.substring(evmAddressString.lastIndexOf(".") + 1));
    }

    /**
     * Get ContractId num from mirror node using evm address.
     *
     * @param evmAddress
     * @return
     */
    Long getContractNum(String evmAddress) {
        JsonObject accountInfoResponse;

        try {
            accountInfoResponse = mirrorNodeGateway.getContractInfo(evmAddress);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error, while processing getContractInfo mirror node query", e);
        }

        String num = accountInfoResponse.get("contract_id").getAsString();

        return Long.parseLong(num.substring(num.lastIndexOf(".") + 1));
    }

    /**
     * Build the list of com.hedera.hashgraph.sdk.proto.TokenBalance for a given account.
     *
     * @param idOrAliasOrEvmAddress
     * @return
     */
    List<TokenBalance> getTokenBalancesForAccount(String idOrAliasOrEvmAddress) {
        JsonObject accountTokensResponse;

        try {
            accountTokensResponse = mirrorNodeGateway.getAccountTokens(idOrAliasOrEvmAddress);
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

    /**
     * Build the list of com.hedera.hashgraph.sdk.proto.TokenRelationship for a given account.
     *
     * @param idOrAliasOrEvmAddress
     * @return
     */
    List<TokenRelationship> getTokenRelationshipsForAccount(String idOrAliasOrEvmAddress) {
        JsonObject accountTokensResponse;
        try {
            accountTokensResponse = mirrorNodeGateway.getAccountTokens(idOrAliasOrEvmAddress);
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
