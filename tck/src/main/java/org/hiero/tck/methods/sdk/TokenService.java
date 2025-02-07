// SPDX-License-Identifier: Apache-2.0
package org.hiero.tck.methods.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.*;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import org.bouncycastle.util.encoders.Hex;
import org.hiero.tck.annotation.JSONRPC2Method;
import org.hiero.tck.annotation.JSONRPC2Service;
import org.hiero.tck.methods.AbstractJSONRPC2Service;
import org.hiero.tck.methods.sdk.param.token.*;
import org.hiero.tck.methods.sdk.response.token.*;
import org.hiero.tck.util.KeyUtils;

/**
 * TokenService for token related methods
 */
@JSONRPC2Service
public class TokenService extends AbstractJSONRPC2Service {

    private static final Duration DEFAULT_GRPC_DEADLINE = Duration.ofSeconds(10L);
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

        params.getCustomFees()
                .ifPresent(customFees ->
                        tokenCreateTransaction.setCustomFees(customFees.get(0).fillOutCustomFees(customFees)));

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

    @JSONRPC2Method("updateTokenFeeSchedule")
    public TokenResponse updateTokenFeeSchedule(TokenUpdateFeeScheduleParams params) throws Exception {
        TokenFeeScheduleUpdateTransaction transaction =
                new TokenFeeScheduleUpdateTransaction().setGrpcDeadline(DEFAULT_GRPC_DEADLINE);

        params.getTokenId().ifPresent(tokenId -> transaction.setTokenId(TokenId.fromString(tokenId)));

        params.getCustomFees()
                .ifPresent(customFees ->
                        transaction.setCustomFees(customFees.get(0).fillOutCustomFees(customFees)));

        params.getCommonTransactionParams()
                .ifPresent(commonParams -> commonParams.fillOutTransaction(transaction, sdkService.getClient()));

        TransactionReceipt receipt = transaction.execute(sdkService.getClient()).getReceipt(sdkService.getClient());

        return new TokenResponse("", receipt.status);
    }

    @JSONRPC2Method("freezeToken")
    public TokenResponse tokenFreezeTransaction(FreezeUnfreezeTokenParams params) throws Exception {
        TokenFreezeTransaction transaction = new TokenFreezeTransaction().setGrpcDeadline(DEFAULT_GRPC_DEADLINE);

        params.getTokenId().ifPresent(tokenId -> transaction.setTokenId(TokenId.fromString(tokenId)));

        params.getAccountId().ifPresent(accountId -> transaction.setAccountId(AccountId.fromString(accountId)));

        params.getCommonTransactionParams()
                .ifPresent(commonParams -> commonParams.fillOutTransaction(transaction, sdkService.getClient()));

        TransactionReceipt receipt = transaction.execute(sdkService.getClient()).getReceipt(sdkService.getClient());

        return new TokenResponse("", receipt.status);
    }

    @JSONRPC2Method("unfreezeToken")
    public TokenResponse tokenUnfreezeTransaction(FreezeUnfreezeTokenParams params) throws Exception {
        TokenUnfreezeTransaction transaction = new TokenUnfreezeTransaction().setGrpcDeadline(DEFAULT_GRPC_DEADLINE);

        params.getTokenId().ifPresent(tokenId -> transaction.setTokenId(TokenId.fromString(tokenId)));

        params.getAccountId().ifPresent(accountId -> transaction.setAccountId(AccountId.fromString(accountId)));

        params.getCommonTransactionParams()
                .ifPresent(commonParams -> commonParams.fillOutTransaction(transaction, sdkService.getClient()));

        TransactionReceipt receipt = transaction.execute(sdkService.getClient()).getReceipt(sdkService.getClient());

        return new TokenResponse("", receipt.status);
    }

    @JSONRPC2Method("associateToken")
    public TokenResponse associateToken(AssociateDisassociateTokenParams params) throws Exception {
        TokenAssociateTransaction transaction = new TokenAssociateTransaction().setGrpcDeadline(DEFAULT_GRPC_DEADLINE);

        params.getTokenIds().ifPresent(tokenIds -> {
            List<TokenId> tokenIdList =
                    tokenIds.stream().map(TokenId::fromString).collect(Collectors.toList());
            transaction.setTokenIds(tokenIdList);
        });

        params.getAccountId().ifPresent(accountId -> transaction.setAccountId(AccountId.fromString(accountId)));

        params.getCommonTransactionParams()
                .ifPresent(commonParams -> commonParams.fillOutTransaction(transaction, sdkService.getClient()));

        TransactionReceipt receipt = transaction.execute(sdkService.getClient()).getReceipt(sdkService.getClient());

        return new TokenResponse("", receipt.status);
    }

    @JSONRPC2Method("dissociateToken")
    public TokenResponse dissociateToken(AssociateDisassociateTokenParams params) throws Exception {
        TokenDissociateTransaction transaction =
                new TokenDissociateTransaction().setGrpcDeadline(DEFAULT_GRPC_DEADLINE);

        params.getTokenIds().ifPresent(tokenIds -> {
            List<TokenId> tokenIdList =
                    tokenIds.stream().map(TokenId::fromString).collect(Collectors.toList());
            transaction.setTokenIds(tokenIdList);
        });

        params.getAccountId().ifPresent(accountId -> transaction.setAccountId(AccountId.fromString(accountId)));

        params.getCommonTransactionParams()
                .ifPresent(commonParams -> commonParams.fillOutTransaction(transaction, sdkService.getClient()));

        TransactionReceipt receipt = transaction.execute(sdkService.getClient()).getReceipt(sdkService.getClient());

        return new TokenResponse("", receipt.status);
    }

    @JSONRPC2Method("pauseToken")
    public TokenResponse pauseToken(PauseUnpauseTokenParams params) throws Exception {
        TokenPauseTransaction transaction = new TokenPauseTransaction().setGrpcDeadline(DEFAULT_GRPC_DEADLINE);

        params.getTokenId().ifPresent(tokenId -> transaction.setTokenId(TokenId.fromString(tokenId)));

        params.getCommonTransactionParams()
                .ifPresent(commonParams -> commonParams.fillOutTransaction(transaction, sdkService.getClient()));

        TransactionReceipt receipt = transaction.execute(sdkService.getClient()).getReceipt(sdkService.getClient());

        return new TokenResponse("", receipt.status);
    }

    @JSONRPC2Method("unpauseToken")
    public TokenResponse tokenUnpauseTransaction(PauseUnpauseTokenParams params) throws Exception {
        TokenUnpauseTransaction transaction = new TokenUnpauseTransaction().setGrpcDeadline(DEFAULT_GRPC_DEADLINE);

        params.getTokenId().ifPresent(tokenId -> transaction.setTokenId(TokenId.fromString(tokenId)));

        params.getCommonTransactionParams()
                .ifPresent(commonParams -> commonParams.fillOutTransaction(transaction, sdkService.getClient()));

        TransactionReceipt receipt = transaction.execute(sdkService.getClient()).getReceipt(sdkService.getClient());

        return new TokenResponse("", receipt.status);
    }

    @JSONRPC2Method("grantTokenKyc")
    public TokenResponse grantTokenKyc(GrantRevokeTokenKycParams params) throws Exception {
        TokenGrantKycTransaction transaction = new TokenGrantKycTransaction().setGrpcDeadline(DEFAULT_GRPC_DEADLINE);

        params.getTokenId().ifPresent(tokenId -> transaction.setTokenId(TokenId.fromString(tokenId)));

        params.getAccountId().ifPresent(accountId -> transaction.setAccountId(AccountId.fromString(accountId)));

        params.getCommonTransactionParams()
                .ifPresent(commonParams -> commonParams.fillOutTransaction(transaction, sdkService.getClient()));

        TransactionReceipt receipt = transaction.execute(sdkService.getClient()).getReceipt(sdkService.getClient());

        return new TokenResponse("", receipt.status);
    }

    @JSONRPC2Method("revokeTokenKyc")
    public TokenResponse revokeTokenKyc(GrantRevokeTokenKycParams params) throws Exception {
        TokenRevokeKycTransaction transaction = new TokenRevokeKycTransaction().setGrpcDeadline(DEFAULT_GRPC_DEADLINE);

        params.getTokenId().ifPresent(tokenId -> transaction.setTokenId(TokenId.fromString(tokenId)));

        params.getAccountId().ifPresent(accountId -> transaction.setAccountId(AccountId.fromString(accountId)));

        params.getCommonTransactionParams()
                .ifPresent(commonParams -> commonParams.fillOutTransaction(transaction, sdkService.getClient()));

        TransactionReceipt receipt = transaction.execute(sdkService.getClient()).getReceipt(sdkService.getClient());

        return new TokenResponse("", receipt.status);
    }

    @JSONRPC2Method("mintToken")
    public TokenMintResponse mintToken(MintTokenParams params) throws Exception {
        TokenMintTransaction transaction = new TokenMintTransaction().setGrpcDeadline(DEFAULT_GRPC_DEADLINE);

        params.getTokenId().ifPresent(tokenId -> transaction.setTokenId(TokenId.fromString(tokenId)));

        try {
            params.getAmount().ifPresent(amount -> transaction.setAmount(Long.parseLong(amount)));
        } catch (NumberFormatException e) {
            transaction.setAmount(-1L);
        }

        params.getMetadata()
                .ifPresent(metadata -> transaction.setMetadata(
                        metadata.stream().map(Hex::decode).toList()));

        params.getCommonTransactionParams()
                .ifPresent(commonParams -> commonParams.fillOutTransaction(transaction, sdkService.getClient()));

        TransactionReceipt receipt = transaction.execute(sdkService.getClient()).getReceipt(sdkService.getClient());

        return new TokenMintResponse(
                "",
                receipt.status,
                receipt.totalSupply.toString(),
                receipt.serials.stream().map(String::valueOf).toList());
    }

    @JSONRPC2Method("burnToken")
    public TokenBurnResponse burnToken(BurnTokenParams params) throws Exception {
        TokenBurnTransaction transaction = new TokenBurnTransaction().setGrpcDeadline(DEFAULT_GRPC_DEADLINE);

        params.getTokenId().ifPresent(tokenId -> transaction.setTokenId(TokenId.fromString(tokenId)));

        try {
            params.getAmount().ifPresent(amount -> transaction.setAmount(Long.parseLong(amount)));
        } catch (NumberFormatException e) {
            transaction.setAmount(-1L);
        }

        params.getSerialNumbers().ifPresent(serialNumbers -> {
            List<Long> tokenIdList = serialNumbers.stream().map(Long::parseLong).collect(Collectors.toList());
            transaction.setSerials(tokenIdList);
        });

        params.getCommonTransactionParams()
                .ifPresent(commonParams -> commonParams.fillOutTransaction(transaction, sdkService.getClient()));

        TransactionReceipt receipt = transaction.execute(sdkService.getClient()).getReceipt(sdkService.getClient());

        return new TokenBurnResponse("", receipt.status, receipt.totalSupply.toString());
    }
}
