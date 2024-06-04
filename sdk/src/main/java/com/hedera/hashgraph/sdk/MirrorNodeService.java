/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2024 Hedera Hashgraph, LLC
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
     * Retrieves the account number from the mirror node using the specified EVM address.
     *
     * @param evmAddress the EVM address for which the account number is to be retrieved
     * @return the account number as a Long
     * @throws RuntimeException if an error occurs while processing the mirror node query
     */
    Long getAccountNum(String evmAddress) {
        JsonObject accountInfoResponse = null;

        try {
            accountInfoResponse = mirrorNodeGateway.getAccountInfo(evmAddress);
        } catch (InterruptedException e) {
           Thread.currentThread().interrupt();
        } catch (IOException e) {
            throw new RuntimeException("Error, while processing getAccountInfo mirror node query", e);
        }

        String num = accountInfoResponse.get("account").getAsString();

        return Long.parseLong(num.substring(num.lastIndexOf(".") + 1));
    }

    /**
     * Retrieves the Ethereum Virtual Machine (EVM) address for an account using the specified account number.
     * This method retrieves the account information from the mirror node and extracts the EVM address.
     *
     * @param num the account number for which the EVM address is to be retrieved
     * @return the EVM address as an instance of EvmAddress
     * @throws RuntimeException if an error occurs while processing the mirror node query
     */
    EvmAddress getAccountEvmAddress(long num) {
        JsonObject accountInfoResponse = null;

        try {
            accountInfoResponse = mirrorNodeGateway.getAccountInfo(String.valueOf(num));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            throw new RuntimeException("Error, while processing getAccountInfo mirror node query", e);
        }

        String evmAddressString = accountInfoResponse.get("evm_address").getAsString();

        return EvmAddress.fromString(
            evmAddressString.substring(evmAddressString.lastIndexOf(".") + 1));
    }

    /**
     * Retrieves the contract number from the mirror node using the specified EVM address.
     *
     * @param evmAddress the EVM address for which the contract number is to be retrieved
     * @return the contract number as a Long
     * @throws RuntimeException if an error occurs while processing the mirror node query
     */
    Long getContractNum(String evmAddress) {
        JsonObject accountInfoResponse = null;

        try {
            accountInfoResponse = mirrorNodeGateway.getContractInfo(evmAddress);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            throw new RuntimeException("Error, while processing getContractInfo mirror node query", e);
        }

        String num = accountInfoResponse.get("contract_id").getAsString();

        return Long.parseLong(num.substring(num.lastIndexOf(".") + 1));
    }

    /**
     * Retrieves the balances of tokens for an account (as a protobuf).
     *
     * @param idOrAliasOrEvmAddress the ID, alias, or EVM address of the account
     * @return a list of TokenBalance (as a protobuf) objects representing the token balances of the account
     * @throws RuntimeException if an error occurs while processing the mirror node query
     */
    List<TokenBalance> getTokenBalancesForAccount(String idOrAliasOrEvmAddress) {
        JsonObject accountTokensResponse = null;

        try {
            accountTokensResponse = mirrorNodeGateway.getAccountTokens(idOrAliasOrEvmAddress);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            throw new RuntimeException("Error, while processing getAccountTokens mirror node query", e);
        }

        JsonArray tokens = accountTokensResponse.get("tokens").getAsJsonArray();

        List<TokenBalance> tokenBalanceList = tokens.asList().stream().map(jsonElement -> {
            var jsonObject = jsonElement.getAsJsonObject();
            var tokenId = jsonObject.get("token_id").getAsString();
            var tokenBalance = jsonObject.get("balance").getAsLong();
            var decimals = jsonObject.get("decimals").getAsInt();

            return TokenBalance.newBuilder().setTokenId(TokenId.fromString(tokenId).toProtobuf())
                .setBalance(tokenBalance)
                .setDecimals(decimals)
                .build();
        }).collect(Collectors.toList());

        return tokenBalanceList;
    }

    /**
     * Retrieves the token relationships for an account (as a protobuf).
     *
     * @param idOrAliasOrEvmAddress the ID, alias, or EVM address of the account
     * @return a list of TokenRelationship (as a protobuf) objects representing the token relationships of the account
     * @throws RuntimeException if an error occurs while processing the mirror node query
     */
    List<TokenRelationship> getTokenRelationshipsForAccount(String idOrAliasOrEvmAddress) {
        JsonObject accountTokensResponse = null;

        try {
            accountTokensResponse = mirrorNodeGateway.getAccountTokens(idOrAliasOrEvmAddress);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            throw new RuntimeException("Error, while processing getAccountTokens mirror node query", e);
        }

        JsonArray tokens = accountTokensResponse.get("tokens").getAsJsonArray();

        List<TokenRelationship> tokenRelationships = tokens.asList().stream().map(jsonElement -> {
            var jsonObject = jsonElement.getAsJsonObject();
            var tokenId = jsonObject.get("token_id").getAsString();
            var balance = jsonObject.get("balance").getAsLong();
            var kycStatus = jsonObject.get("kyc_status").getAsString();
            var freezeStatus = jsonObject.get("freeze_status").getAsString();
            var decimals = jsonObject.get("decimals").getAsInt();
            var automaticAssociation = jsonObject.get("automatic_association").getAsBoolean();

            return TokenRelationship.newBuilder()
                .setTokenId(TokenId.fromString(tokenId).toProtobuf())
                .setBalance(balance)
                .setKycStatus(getTokenKycStatusFromString(kycStatus))
                .setFreezeStatus(getTokenFreezeStatusFromString(freezeStatus))
                .setDecimals(decimals)
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
