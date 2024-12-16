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

package com.hiero.tck.methods.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hiero.sdk.AccountId;
import com.hiero.sdk.CustomFixedFee;
import com.hiero.sdk.CustomFractionalFee;
import com.hiero.sdk.CustomRoyaltyFee;
import com.hiero.sdk.Status;
import com.hiero.sdk.TokenCreateTransaction;
import com.hiero.sdk.TokenDeleteTransaction;
import com.hiero.sdk.TokenId;
import com.hiero.sdk.TokenSupplyType;
import com.hiero.sdk.TokenType;
import com.hiero.sdk.TokenUpdateTransaction;
import com.hiero.sdk.TransactionReceipt;
import com.hiero.tck.annotation.JSONRPC2Method;
import com.hiero.tck.annotation.JSONRPC2Service;
import com.hiero.tck.methods.AbstractJSONRPC2Service;
import com.hiero.tck.methods.sdk.param.TokenCreateParams;
import com.hiero.tck.methods.sdk.param.TokenDeleteParams;
import com.hiero.tck.methods.sdk.param.TokenUpdateParams;
import com.hiero.tck.methods.sdk.response.TokenResponse;
import com.hiero.tck.util.KeyUtils;
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

        params.getWipeKey().ifPresent(key -> {
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
            List<com.hiero.sdk.CustomFee> customFeeList = new ArrayList<>();
            for (var customFee : customFees) {
                // set fixed fees
                customFee.getFixedFee().ifPresent(fixedFee -> {
                    var sdkFixedFee = new CustomFixedFee()
                            .setAmount(Long.parseLong(fixedFee.getAmount()))
                            .setFeeCollectorAccountId(AccountId.fromString(customFee.getFeeCollectorAccountId()))
                            .setAllCollectorsAreExempt(customFee.getFeeCollectorsExempt());
                    fixedFee.getDenominatingTokenId()
                            .ifPresent(tokenID -> sdkFixedFee.setDenominatingTokenId(TokenId.fromString(tokenID)));
                    customFeeList.add(sdkFixedFee);
                });

                // set fractional fees
                customFee.getFractionalFee().ifPresent(fractionalFee -> {
                    var sdkFractionalFee = new CustomFractionalFee()
                            .setNumerator(Long.parseLong(fractionalFee.getNumerator()))
                            .setDenominator(Long.parseLong(fractionalFee.getDenominator()))
                            .setMin(Long.parseLong(fractionalFee.getMinimumAmount()))
                            .setMax(Long.parseLong(fractionalFee.getMaximumAmount()))
                            .setFeeCollectorAccountId(AccountId.fromString(customFee.getFeeCollectorAccountId()))
                            .setAllCollectorsAreExempt(customFee.getFeeCollectorsExempt());

                    customFeeList.add(sdkFractionalFee);
                });

                // set royalty fees
                customFee.getRoyaltyFee().ifPresent(royaltyFee -> {
                    var sdkRoyaltyFee = new CustomRoyaltyFee()
                            .setDenominator(Long.parseLong(royaltyFee.getDenominator()))
                            .setNumerator(Long.parseLong(royaltyFee.getNumerator()))
                            .setFeeCollectorAccountId(AccountId.fromString(customFee.getFeeCollectorAccountId()))
                            .setAllCollectorsAreExempt(customFee.getFeeCollectorsExempt());

                    royaltyFee.getFallbackFee().ifPresent(fallbackFee -> {
                        var fixedFallback = new CustomFixedFee().setAmount(Long.parseLong(fallbackFee.getAmount()));
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

    @JSONRPC2Method("updateToken")
    public TokenResponse updateToken(final TokenUpdateParams params) throws Exception {
        TokenUpdateTransaction tokenUpdateTransaction = new TokenUpdateTransaction();

        params.getTokenId().ifPresent(tokenId -> tokenUpdateTransaction.setTokenId(TokenId.fromString(tokenId)));

        params.getAdminKey().ifPresent(key -> {
            try {
                tokenUpdateTransaction.setAdminKey(KeyUtils.getKeyFromString(key));
            } catch (InvalidProtocolBufferException e) {
                throw new IllegalArgumentException(e);
            }
        });

        params.getKycKey().ifPresent(key -> {
            try {
                tokenUpdateTransaction.setKycKey(KeyUtils.getKeyFromString(key));
            } catch (InvalidProtocolBufferException e) {
                throw new IllegalArgumentException(e);
            }
        });

        params.getFreezeKey().ifPresent(key -> {
            try {
                tokenUpdateTransaction.setFreezeKey(KeyUtils.getKeyFromString(key));
            } catch (InvalidProtocolBufferException e) {
                throw new IllegalArgumentException(e);
            }
        });

        params.getWipeKey().ifPresent(key -> {
            try {
                tokenUpdateTransaction.setWipeKey(KeyUtils.getKeyFromString(key));
            } catch (InvalidProtocolBufferException e) {
                throw new IllegalArgumentException(e);
            }
        });

        params.getSupplyKey().ifPresent(key -> {
            try {
                tokenUpdateTransaction.setSupplyKey(KeyUtils.getKeyFromString(key));
            } catch (InvalidProtocolBufferException e) {
                throw new IllegalArgumentException(e);
            }
        });

        params.getFeeScheduleKey().ifPresent(key -> {
            try {
                tokenUpdateTransaction.setFeeScheduleKey(KeyUtils.getKeyFromString(key));
            } catch (InvalidProtocolBufferException e) {
                throw new IllegalArgumentException(e);
            }
        });

        params.getPauseKey().ifPresent(key -> {
            try {
                tokenUpdateTransaction.setPauseKey(KeyUtils.getKeyFromString(key));
            } catch (InvalidProtocolBufferException e) {
                throw new IllegalArgumentException(e);
            }
        });

        params.getMetadataKey().ifPresent(key -> {
            try {
                tokenUpdateTransaction.setMetadataKey(KeyUtils.getKeyFromString(key));
            } catch (InvalidProtocolBufferException e) {
                throw new IllegalArgumentException(e);
            }
        });

        params.getName().ifPresent(tokenUpdateTransaction::setTokenName);
        params.getSymbol().ifPresent(tokenUpdateTransaction::setTokenSymbol);

        params.getTreasuryAccountId()
                .ifPresent(treasuryAccountId ->
                        tokenUpdateTransaction.setTreasuryAccountId(AccountId.fromString(treasuryAccountId)));

        params.getExpirationTime()
                .ifPresent(expirationTime ->
                        tokenUpdateTransaction.setExpirationTime(Duration.ofSeconds(Long.parseLong(expirationTime))));

        params.getAutoRenewAccountId()
                .ifPresent(autoRenewAccountId ->
                        tokenUpdateTransaction.setAutoRenewAccountId(AccountId.fromString(autoRenewAccountId)));

        params.getAutoRenewPeriod()
                .ifPresent(autoRenewPeriodSeconds -> tokenUpdateTransaction.setAutoRenewPeriod(
                        Duration.ofSeconds(Long.parseLong(autoRenewPeriodSeconds))));

        params.getMemo().ifPresent(tokenUpdateTransaction::setTokenMemo);

        params.getMetadata().ifPresent(metadata -> tokenUpdateTransaction.setTokenMetadata(metadata.getBytes()));

        params.getCommonTransactionParams()
                .ifPresent(commonTransactionParams ->
                        commonTransactionParams.fillOutTransaction(tokenUpdateTransaction, sdkService.getClient()));

        TransactionReceipt transactionReceipt =
                tokenUpdateTransaction.execute(sdkService.getClient()).getReceipt(sdkService.getClient());

        return new TokenResponse("", transactionReceipt.status);
    }

    @JSONRPC2Method("deleteToken")
    public TokenResponse deleteToken(final TokenDeleteParams params) throws Exception {
        TokenDeleteTransaction tokenDeleteTransaction = new TokenDeleteTransaction();

        params.getTokenId().ifPresent(tokenId -> tokenDeleteTransaction.setTokenId(TokenId.fromString(tokenId)));

        params.getCommonTransactionParams()
                .ifPresent(commonTransactionParams ->
                        commonTransactionParams.fillOutTransaction(tokenDeleteTransaction, sdkService.getClient()));

        TransactionReceipt transactionReceipt =
                tokenDeleteTransaction.execute(sdkService.getClient()).getReceipt(sdkService.getClient());

        return new TokenResponse("", transactionReceipt.status);
    }
}
