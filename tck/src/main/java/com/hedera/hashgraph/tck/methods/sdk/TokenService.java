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

package com.hedera.hashgraph.tck.methods.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.CustomFixedFee;
import com.hedera.hashgraph.sdk.CustomFractionalFee;
import com.hedera.hashgraph.sdk.CustomRoyaltyFee;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenId;
import com.hedera.hashgraph.sdk.TokenSupplyType;
import com.hedera.hashgraph.sdk.TokenType;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.tck.annotation.JSONRPC2Method;
import com.hedera.hashgraph.tck.annotation.JSONRPC2Service;
import com.hedera.hashgraph.tck.methods.AbstractJSONRPC2Service;
import com.hedera.hashgraph.tck.methods.sdk.param.TokenCreateParams;
import com.hedera.hashgraph.tck.methods.sdk.response.TokenResponse;
import com.hedera.hashgraph.tck.util.KeyUtils;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * TokenService for token related methods
 */
@JSONRPC2Service
public class TokenService extends AbstractJSONRPC2Service {

    private final SdkService sdkService;

    public TokenService(SdkService sdkService) {
        this.sdkService = sdkService;
    }

    @JSONRPC2Method("createToken")
    public TokenResponse createToken(final TokenCreateParams params) throws Exception {
        TokenCreateTransaction tokenCreateTransaction = new TokenCreateTransaction();

        params.getAdminKey().ifPresent(key -> {
            try {
                tokenCreateTransaction.setAdminKey(KeyUtils.getKeyFromString(key));
            } catch (InvalidProtocolBufferException e) {
                throw new IllegalArgumentException(e);
            }
        });

        params.getKycKey().ifPresent(key -> {
            try {
                tokenCreateTransaction.setKycKey(KeyUtils.getKeyFromString(key));
            } catch (InvalidProtocolBufferException e) {
                throw new IllegalArgumentException(e);
            }
        });

        params.getFreezeKey().ifPresent(key -> {
            try {
                tokenCreateTransaction.setFreezeKey(KeyUtils.getKeyFromString(key));
            } catch (InvalidProtocolBufferException e) {
                throw new IllegalArgumentException(e);
            }
        });

        params.getFreezeKey().ifPresent(key -> {
            try {
                tokenCreateTransaction.setWipeKey(KeyUtils.getKeyFromString(key));
            } catch (InvalidProtocolBufferException e) {
                throw new IllegalArgumentException(e);
            }
        });

        params.getSupplyKey().ifPresent(key -> {
            try {
                tokenCreateTransaction.setSupplyKey(KeyUtils.getKeyFromString(key));
            } catch (InvalidProtocolBufferException e) {
                throw new IllegalArgumentException(e);
            }
        });

        params.getFeeScheduleKey().ifPresent(key -> {
            try {
                tokenCreateTransaction.setFeeScheduleKey(KeyUtils.getKeyFromString(key));
            } catch (InvalidProtocolBufferException e) {
                throw new IllegalArgumentException(e);
            }
        });

        params.getPauseKey().ifPresent(key -> {
            try {
                tokenCreateTransaction.setPauseKey(KeyUtils.getKeyFromString(key));
            } catch (InvalidProtocolBufferException e) {
                throw new IllegalArgumentException(e);
            }
        });

        params.getMetadataKey().ifPresent(key -> {
            try {
                tokenCreateTransaction.setMetadataKey(KeyUtils.getKeyFromString(key));
            } catch (InvalidProtocolBufferException e) {
                throw new IllegalArgumentException(e);
            }
        });

        params.getName().ifPresent(tokenCreateTransaction::setTokenName);
        params.getSymbol().ifPresent(tokenCreateTransaction::setTokenSymbol);
        params.getDecimals().ifPresent(decimals -> tokenCreateTransaction.setDecimals(decimals.intValue()));
        params.getInitialSupply()
                .ifPresent(initialSupply -> tokenCreateTransaction.setInitialSupply(Long.parseLong(initialSupply)));

        params.getTreasuryAccountId()
                .ifPresent(treasuryAccountId ->
                        tokenCreateTransaction.setTreasuryAccountId(AccountId.fromString(treasuryAccountId)));

        params.getFreezeDefault().ifPresent(tokenCreateTransaction::setFreezeDefault);

        params.getExpirationTime()
                .ifPresent(expirationTime ->
                        tokenCreateTransaction.setExpirationTime(Duration.ofSeconds(Long.parseLong(expirationTime))));

        params.getAutoRenewAccountId()
                .ifPresent(autoRenewAccountId ->
                        tokenCreateTransaction.setAutoRenewAccountId(AccountId.fromString(autoRenewAccountId)));

        params.getAutoRenewPeriod()
                .ifPresent(autoRenewPeriodSeconds -> tokenCreateTransaction.setAutoRenewPeriod(
                        Duration.ofSeconds(Long.parseLong(autoRenewPeriodSeconds))));

        params.getMemo().ifPresent(tokenCreateTransaction::setTokenMemo);
        params.getTokenType().ifPresent(tokenType -> {
            if (tokenType.equals("ft")) {
                tokenCreateTransaction.setTokenType(TokenType.FUNGIBLE_COMMON);
            } else if (tokenType.equals("nft")) {
                tokenCreateTransaction.setTokenType(TokenType.NON_FUNGIBLE_UNIQUE);
            } else {
                throw new IllegalArgumentException("Invalid token type");
            }
        });

        params.getSupplyType().ifPresent(supplyType -> {
            if (supplyType.equals("infinite")) {
                tokenCreateTransaction.setSupplyType(TokenSupplyType.INFINITE);
            } else if (supplyType.equals("finite")) {
                tokenCreateTransaction.setSupplyType(TokenSupplyType.FINITE);
            } else {
                throw new IllegalArgumentException("Invalid supply type");
            }
        });

        params.getMaxSupply().ifPresent(maxSupply -> tokenCreateTransaction.setMaxSupply(Long.valueOf(maxSupply)));

        params.getCustomFees().ifPresent(customFees -> {
            List<com.hedera.hashgraph.sdk.CustomFee> customFeeList = new ArrayList<>();
            for (var customFee : customFees) {
                // set fixed fees
                customFee.getFixedFee().ifPresent(fixedFee -> {
                    var sdkFixedFee = new CustomFixedFee()
                            .setAmount(fixedFee.getAmount())
                            .setFeeCollectorAccountId(AccountId.fromString(customFee.getFeeCollectorAccountId()))
                            .setAllCollectorsAreExempt(customFee.getFeeCollectorsExempt());
                    fixedFee.getDenominatingTokenId()
                            .ifPresent(tokenID -> sdkFixedFee.setDenominatingTokenId(TokenId.fromString(tokenID)));
                    customFeeList.add(sdkFixedFee);
                });

                // set fractional fees
                customFee.getFractionalFee().ifPresent(fractionalFee -> {
                    var sdkFractionalFee = new CustomFractionalFee()
                            .setNumerator(fractionalFee.getNumerator())
                            .setDenominator(fractionalFee.getDenominator())
                            .setMin(fractionalFee.getMinimumAmount())
                            .setMax(fractionalFee.getMaximumAmount())
                            .setFeeCollectorAccountId(AccountId.fromString(customFee.getFeeCollectorAccountId()))
                            .setAllCollectorsAreExempt(customFee.getFeeCollectorsExempt());

                    customFeeList.add(sdkFractionalFee);
                });

                // set royalty fees
                customFee.getRoyaltyFee().ifPresent(royaltyFee -> {
                    var sdkRoyaltyFee = new CustomRoyaltyFee()
                            .setDenominator(royaltyFee.getDenominator())
                            .setNumerator(royaltyFee.getNumerator())
                            .setFeeCollectorAccountId(AccountId.fromString(customFee.getFeeCollectorAccountId()))
                            .setAllCollectorsAreExempt(customFee.getFeeCollectorsExempt());

                    royaltyFee.getFallbackFee().ifPresent(fallbackFee -> {
                        var fixedFallback = new CustomFixedFee().setAmount(fallbackFee.getAmount());
                        fallbackFee
                                .getDenominatingTokenId()
                                .ifPresent(
                                        tokenID -> fixedFallback.setDenominatingTokenId(TokenId.fromString(tokenID)));
                        sdkRoyaltyFee.setFallbackFee(fixedFallback);
                    });
                    customFeeList.add(sdkRoyaltyFee);
                });
            }
            tokenCreateTransaction.setCustomFees(customFeeList);
        });

        params.getMetadata().ifPresent(metadata -> tokenCreateTransaction.setTokenMetadata(metadata.getBytes()));

        params.getCommonTransactionParams()
                .ifPresent(commonTransactionParams ->
                        commonTransactionParams.fillOutTransaction(tokenCreateTransaction, sdkService.getClient()));

        TransactionReceipt transactionReceipt =
                tokenCreateTransaction.execute(sdkService.getClient()).getReceipt(sdkService.getClient());

        String tokenId = "";
        if (transactionReceipt.status == Status.SUCCESS) {
            tokenId = transactionReceipt.tokenId.toString();
        }

        return new TokenResponse(tokenId, transactionReceipt.status);
    }
}
