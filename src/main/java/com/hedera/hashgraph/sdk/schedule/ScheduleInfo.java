package com.hedera.hashgraph.sdk.schedule;

import com.google.common.base.MoreObjects;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.proto.Key;
import com.hedera.hashgraph.proto.ScheduleGetInfoResponse;
import com.hedera.hashgraph.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.proto.TransactionReceipt;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.KeyList;
import com.hedera.hashgraph.sdk.crypto.PublicKey;

import javax.annotation.Nullable;
import java.time.Instant;

public class ScheduleInfo {
    /**
     * The ID of the Scheduled Entity
     */
    public final ScheduleId scheduleId;

    /**
     * The Schedule ID which created the Scheduled TX
     */
    public final AccountId creatorAccountId;

    /**
     * The account which is going to pay for the execution of the Scheduled TX
     */
    @Nullable
    public final AccountId payerAccountId;

    /**
     * The transaction serialized into bytes that must be signed
     */
    public final SchedulableTransactionBody schedulableTransactionBody;

    /**
     * The signatories that have provided signatures so far for the Scheduled TX
     */
    public final KeyList signatories;

    /**
     * The Key which is able to delete the Scheduled Transaction if set
     */
    @Nullable
    public final PublicKey adminKey;

    /**
     * Publicly visible information about the Scheduled entity, up to 100 bytes. No guarantee of uniqueness.
     */
    public final String memo;

    /**
     * The epoch second at which the schedule will expire
     */
    public final Instant expirationTime;

    public final Instant executionTime;

    public final Instant deletionTime;

    public final TransactionId scheduledTransactionId;

    ScheduleInfo(com.hedera.hashgraph.proto.ScheduleInfo info) {
        Instant execTime = null;
        Instant deleteTime = null;
        switch (info.getDataCase()) {
            case EXECUTION_TIME:
                execTime = info.hasExecutionTime() ? TimestampHelper.timestampTo(info.getExecutionTime()) : null;
            case DELETION_TIME:
                deleteTime = info.hasExecutionTime() ? TimestampHelper.timestampTo(info.getExecutionTime()) : null;
        }
        scheduleId = new ScheduleId(info.getScheduleID());
        creatorAccountId = new AccountId(info.getCreatorAccountID());
        payerAccountId = new AccountId(info.getPayerAccountID());
        schedulableTransactionBody = info.getScheduledTransactionBody();
        executionTime = execTime;
        deletionTime = deleteTime;
        signatories = info.hasSigners() ? (KeyList) PublicKey.fromProtoKey(Key.newBuilder().setKeyList(info.getSigners())) : null;
        adminKey = info.hasAdminKey() ? PublicKey.fromProtoKey(info.getAdminKey()) : null;
        memo = info.getMemo();
        expirationTime = info.hasExpirationTime() ? TimestampHelper.timestampTo(info.getExpirationTime()) : null;
        scheduledTransactionId = new TransactionId(info.getScheduledTransactionID());
    }

    static ScheduleInfo fromResponse(Response response) {
        if (!response.hasScheduleGetInfo()) {
            throw new IllegalArgumentException("query response was not `ScheduleGetInfoResponse`");
        }

        ScheduleGetInfoResponse infoResponse = response.getScheduleGetInfo();

        return new ScheduleInfo(infoResponse.getScheduleInfo());
    }

    public Transaction getTransaction() throws InvalidProtocolBufferException  {
        com.hedera.hashgraph.proto.TransactionBody.Builder transactionBody = com.hedera.hashgraph.proto.TransactionBody.newBuilder();
        transactionBody.setMemo(schedulableTransactionBody.getMemo());
        transactionBody.setTransactionFee(schedulableTransactionBody.getTransactionFee());


        switch (schedulableTransactionBody.getDataCase()) {
            case CONTRACTCALL:
                transactionBody.setContractCall(schedulableTransactionBody.getContractCall());

            case CONTRACTCREATEINSTANCE:
                transactionBody.setContractCreateInstance(schedulableTransactionBody.getContractCreateInstance());

            case CONTRACTUPDATEINSTANCE:
                transactionBody.setContractUpdateInstance(schedulableTransactionBody.getContractUpdateInstance());

            case CONTRACTDELETEINSTANCE:
                transactionBody.setContractDeleteInstance(schedulableTransactionBody.getContractDeleteInstance());

            case CRYPTOCREATEACCOUNT:
                transactionBody.setCryptoCreateAccount(schedulableTransactionBody.getCryptoCreateAccount());

            case CRYPTODELETE:
                transactionBody.setCryptoDelete(schedulableTransactionBody.getCryptoDelete());

            case CRYPTOTRANSFER:
                transactionBody.setCryptoTransfer(schedulableTransactionBody.getCryptoTransfer());

            case CRYPTOUPDATEACCOUNT:
                transactionBody.setCryptoUpdateAccount(schedulableTransactionBody.getCryptoUpdateAccount());

            case FILEAPPEND:
                transactionBody.setFileAppend(schedulableTransactionBody.getFileAppend());

            case FILECREATE:
                transactionBody.setFileCreate(schedulableTransactionBody.getFileCreate());

            case FILEDELETE:
                transactionBody.setFileDelete(schedulableTransactionBody.getFileDelete());

            case FILEUPDATE:
                transactionBody.setFileUpdate(schedulableTransactionBody.getFileUpdate());

            case SYSTEMDELETE:
                transactionBody.setSystemDelete(schedulableTransactionBody.getSystemDelete());

            case SYSTEMUNDELETE:
                transactionBody.setSystemUndelete(schedulableTransactionBody.getSystemUndelete());

            case FREEZE:
                transactionBody.setFreeze(schedulableTransactionBody.getFreeze());

            case CONSENSUSCREATETOPIC:
                transactionBody.setConsensusCreateTopic(schedulableTransactionBody.getConsensusCreateTopic());

            case CONSENSUSUPDATETOPIC:
                transactionBody.setConsensusUpdateTopic(schedulableTransactionBody.getConsensusUpdateTopic());

            case CONSENSUSDELETETOPIC:
                transactionBody.setConsensusDeleteTopic(schedulableTransactionBody.getConsensusDeleteTopic());

            case CONSENSUSSUBMITMESSAGE:
                transactionBody.setConsensusSubmitMessage(schedulableTransactionBody.getConsensusSubmitMessage());

            case TOKENASSOCIATE:
                transactionBody.setTokenAssociate(schedulableTransactionBody.getTokenAssociate());

            case TOKENBURN:
                transactionBody.setTokenBurn(schedulableTransactionBody.getTokenBurn());

            case TOKENCREATION:
                transactionBody.setTokenCreation(schedulableTransactionBody.getTokenCreation());

            case TOKENDELETION:
                transactionBody.setTokenDeletion(schedulableTransactionBody.getTokenDeletion());

            case TOKENDISSOCIATE:
                transactionBody.setTokenDissociate(schedulableTransactionBody.getTokenDissociate());

            case TOKENFREEZE:
                transactionBody.setTokenFreeze(schedulableTransactionBody.getTokenFreeze());

            case TOKENGRANTKYC:
                transactionBody.setTokenGrantKyc(schedulableTransactionBody.getTokenGrantKyc());

            case TOKENMINT:
                transactionBody.setTokenMint(schedulableTransactionBody.getTokenMint());

            case TOKENREVOKEKYC:
                transactionBody.setTokenRevokeKyc(schedulableTransactionBody.getTokenRevokeKyc());

            case TOKENUNFREEZE:
                transactionBody.setTokenUnfreeze(schedulableTransactionBody.getTokenUnfreeze());

            case TOKENUPDATE:
                transactionBody.setTokenUpdate(schedulableTransactionBody.getTokenUpdate());

            case TOKENWIPE:
                transactionBody.setTokenWipe(schedulableTransactionBody.getTokenWipe());

            case SCHEDULEDELETE:
                transactionBody.setScheduleDelete(schedulableTransactionBody.getScheduleDelete());
        }

        return Transaction.fromBytes(com.hedera.hashgraph.proto.Transaction.newBuilder()
            .setBodyBytes(transactionBody.build().toByteString())
            .build()
            .toByteArray());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("scheduleId", scheduleId)
            .add("creatorAccountId", creatorAccountId)
            .add("payerAccountId", payerAccountId)
            .add("signatories", signatories)
            .add("adminKey", adminKey)
            .add("memo", memo)
            .add("expirationTime", expirationTime)
            .toString();
    }
}
