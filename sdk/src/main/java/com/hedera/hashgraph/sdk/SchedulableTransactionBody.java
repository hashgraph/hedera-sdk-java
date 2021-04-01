package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

import javax.annotation.Nullable;

public class SchedulableTransactionBody {
    @Nullable
    public final Long transactionFee;
    @Nullable
    public final String memo;
    @Nullable
    public final Transaction<?> schedulableTransaction;

    SchedulableTransactionBody(@Nullable Long transactionFee, @Nullable String memo, @Nullable Transaction<?> transaction){
        this.transactionFee = transactionFee;
        this.memo = memo;
        this.schedulableTransaction = transaction;
    }

    SchedulableTransactionBody(TransactionBody body){
        this.transactionFee = body.getTransactionFee();
        this.memo = body.getMemo();
        this.schedulableTransaction = getTransaction(body);
    }

    Transaction<?> getTransaction(TransactionBody body) {
        try{
            switch (body.getDataCase()) {
                case CONTRACTCALL:
                    return new ContractExecuteTransaction(body);

                case CONTRACTCREATEINSTANCE:
                    return new ContractCreateTransaction(body);

                case CONTRACTUPDATEINSTANCE:
                    return new ContractUpdateTransaction(body);

                case CONTRACTDELETEINSTANCE:
                    return new ContractDeleteTransaction(body);

                case CRYPTOCREATEACCOUNT:
                    return new AccountCreateTransaction(body);

                case CRYPTODELETE:
                    return new AccountDeleteTransaction(body);

                case CRYPTOTRANSFER:
                    return new TransferTransaction(body);

                case CRYPTOUPDATEACCOUNT:
                    return new AccountUpdateTransaction(body);

                case FILEAPPEND:
                    return new FileAppendTransaction(body);

                case FILECREATE:
                    return new FileCreateTransaction(body);

                case FILEDELETE:
                    return new FileDeleteTransaction(body);

                case FILEUPDATE:
                    return new FileUpdateTransaction(body);

                case SYSTEMDELETE:
                    return new SystemUndeleteTransaction(body);

                case SYSTEMUNDELETE:
                    return new SystemDeleteTransaction(body);

                case FREEZE:
                    return new FreezeTransaction(body);

                case CONSENSUSCREATETOPIC:
                    return new TopicCreateTransaction(body);

                case CONSENSUSUPDATETOPIC:
                    return new TopicUpdateTransaction(body);

                case CONSENSUSDELETETOPIC:
                    return new TopicDeleteTransaction(body);

                case CONSENSUSSUBMITMESSAGE:
                    return new TopicMessageSubmitTransaction(body);

                case TOKENASSOCIATE:
                    return new TokenAssociateTransaction(body);

                case TOKENBURN:
                    return new TokenBurnTransaction(body);

                case TOKENCREATION:
                    return new TokenCreateTransaction(body);

                case TOKENDELETION:
                    return new TokenDeleteTransaction(body);

                case TOKENDISSOCIATE:
                    return new TokenDissociateTransaction(body);

                case TOKENFREEZE:
                    return new TokenFreezeTransaction(body);

                case TOKENGRANTKYC:
                    return new TokenGrantKycTransaction(body);

                case TOKENMINT:
                    return new TokenMintTransaction(body);

                case TOKENREVOKEKYC:
                    return new TokenRevokeKycTransaction(body);

                case TOKENUNFREEZE:
                    return new TokenUnfreezeTransaction(body);

                case TOKENUPDATE:
                    return new TokenUpdateTransaction(body);

                case TOKENWIPE:
                    return new TokenWipeTransaction(body);

                case SCHEDULEDELETE:
                    return new ScheduleDeleteTransaction(body);

                default:
                    throw new IllegalArgumentException("parsed transaction body has no data");
            }
        } catch (InvalidProtocolBufferException e){
            return null;
        }

    }

    static SchedulableTransactionBody fromProtobuf(com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody body) throws InvalidProtocolBufferException {
        var dataCase = body.getDataCase();

        var txBody = TransactionBody.newBuilder();

        switch (dataCase) {
            case CONTRACTCALL:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new ContractExecuteTransaction(txBody.setContractCall(body.getContractCall()).build()));

            case CONTRACTCREATEINSTANCE:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new ContractCreateTransaction(txBody.setContractCreateInstance(body.getContractCreateInstance()).build()));

            case CONTRACTUPDATEINSTANCE:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new ContractUpdateTransaction(txBody.setContractUpdateInstance(body.getContractUpdateInstance()).build()));

            case CONTRACTDELETEINSTANCE:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new ContractDeleteTransaction(txBody.setContractDeleteInstance(body.getContractDeleteInstance()).build()));

            case CRYPTOCREATEACCOUNT:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new AccountCreateTransaction(txBody.setCryptoCreateAccount(body.getCryptoCreateAccount()).build()));

            case CRYPTODELETE:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new AccountDeleteTransaction(txBody.setCryptoDelete(body.getCryptoDelete()).build()));

            case CRYPTOTRANSFER:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new TransferTransaction(txBody.setCryptoTransfer(body.getCryptoTransfer()).build()));

            case CRYPTOUPDATEACCOUNT:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new AccountUpdateTransaction(txBody.setCryptoUpdateAccount(body.getCryptoUpdateAccount()).build()));

            case FILEAPPEND:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new FileAppendTransaction(txBody.setFileAppend(body.getFileAppend()).build()));

            case FILECREATE:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new FileCreateTransaction(txBody.setFileCreate(body.getFileCreate()).build()));

            case FILEDELETE:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new FileDeleteTransaction(txBody.setFileDelete(body.getFileDelete()).build()));

            case FILEUPDATE:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new FileUpdateTransaction(txBody.setFileUpdate(body.getFileUpdate()).build()));

            case SYSTEMDELETE:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new SystemUndeleteTransaction(txBody.setSystemDelete(body.getSystemDelete()).build()));

            case SYSTEMUNDELETE:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new SystemDeleteTransaction(txBody.setSystemUndelete(body.getSystemUndelete()).build()));

            case FREEZE:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new FreezeTransaction(txBody.setFreeze(body.getFreeze()).build()));

            case CONSENSUSCREATETOPIC:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new TopicCreateTransaction(txBody.setConsensusCreateTopic(body.getConsensusCreateTopic()).build()));

            case CONSENSUSUPDATETOPIC:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new TopicUpdateTransaction(txBody.setConsensusUpdateTopic(body.getConsensusUpdateTopic()).build()));

            case CONSENSUSDELETETOPIC:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new TopicDeleteTransaction(txBody.setConsensusDeleteTopic(body.getConsensusDeleteTopic()).build()));

            case CONSENSUSSUBMITMESSAGE:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new TopicMessageSubmitTransaction(txBody.setConsensusSubmitMessage(body.getConsensusSubmitMessage()).build()));

            case TOKENASSOCIATE:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new TokenAssociateTransaction(txBody.setTokenAssociate(body.getTokenAssociate()).build()));

            case TOKENBURN:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new TokenBurnTransaction(txBody.setTokenBurn(body.getTokenBurn()).build()));

            case TOKENCREATION:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new TokenCreateTransaction(txBody.setTokenCreation(body.getTokenCreation()).build()));

            case TOKENDELETION:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new TokenDeleteTransaction(txBody.setTokenDeletion(body.getTokenDeletion()).build()));

            case TOKENDISSOCIATE:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new TokenDissociateTransaction(txBody.setTokenDissociate(body.getTokenDissociate()).build()));

            case TOKENFREEZE:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new TokenFreezeTransaction(txBody.setTokenFreeze(body.getTokenFreeze()).build()));

            case TOKENGRANTKYC:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new TokenGrantKycTransaction(txBody.setTokenGrantKyc(body.getTokenGrantKyc()).build()));

            case TOKENMINT:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new TokenMintTransaction(txBody.setTokenMint(body.getTokenMint()).build()));

            case TOKENREVOKEKYC:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new TokenRevokeKycTransaction(txBody.setTokenRevokeKyc(body.getTokenRevokeKyc()).build()));

            case TOKENUNFREEZE:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new TokenUnfreezeTransaction(txBody.setTokenUnfreeze(body.getTokenUnfreeze()).build()));

            case TOKENUPDATE:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new TokenUpdateTransaction(txBody.setTokenUpdate(body.getTokenUpdate()).build()));

            case TOKENWIPE:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new TokenWipeTransaction(txBody.setTokenWipe(body.getTokenWipe()).build()));

            case SCHEDULEDELETE:
                return new SchedulableTransactionBody(body.getTransactionFee(), body.getMemo(),
                    new ScheduleDeleteTransaction(txBody.setScheduleDelete(body.getScheduleDelete()).build()));

            default:
                throw new IllegalArgumentException("parsed transaction body has no data");
        }
    }

    com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody toProtobuf() {
        var scheduleableTransactionBodyBuilder = com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody.newBuilder();
        scheduleableTransactionBodyBuilder.setMemo(this.memo);
        scheduleableTransactionBodyBuilder.setTransactionFee(this.transactionFee);
        var txBody = this.schedulableTransaction.bodyBuilder;
        @Var TransactionBody.DataCase dataCase = txBody.getDataCase();

        switch (dataCase) {
            case CONTRACTCALL:
                return scheduleableTransactionBodyBuilder.setContractCall(txBody.getContractCall()).build();

            case CONTRACTCREATEINSTANCE:
                return scheduleableTransactionBodyBuilder.setContractCreateInstance(txBody.getContractCreateInstance()).build();

            case CONTRACTUPDATEINSTANCE:
                return scheduleableTransactionBodyBuilder.setContractUpdateInstance(txBody.getContractUpdateInstance()).build();

            case CONTRACTDELETEINSTANCE:
                return scheduleableTransactionBodyBuilder.setContractDeleteInstance(txBody.getContractDeleteInstance()).build();

            case CRYPTOCREATEACCOUNT:
                return scheduleableTransactionBodyBuilder.setCryptoCreateAccount(txBody.getCryptoCreateAccount()).build();

            case CRYPTODELETE:
                return scheduleableTransactionBodyBuilder.setCryptoDelete(txBody.getCryptoDelete()).build();

            case CRYPTOTRANSFER:
                return scheduleableTransactionBodyBuilder.setCryptoTransfer(txBody.getCryptoTransfer()).build();

            case CRYPTOUPDATEACCOUNT:
                return scheduleableTransactionBodyBuilder.setCryptoUpdateAccount(txBody.getCryptoUpdateAccount()).build();

            case FILEAPPEND:
                return scheduleableTransactionBodyBuilder.setFileAppend(txBody.getFileAppend()).build();

            case FILECREATE:
                return scheduleableTransactionBodyBuilder.setFileCreate(txBody.getFileCreate()).build();

            case FILEDELETE:
                return scheduleableTransactionBodyBuilder.setFileDelete(txBody.getFileDelete()).build();

            case FILEUPDATE:
                return scheduleableTransactionBodyBuilder.setFileUpdate(txBody.getFileUpdate()).build();

            case SYSTEMDELETE:
                return scheduleableTransactionBodyBuilder.setSystemDelete(txBody.getSystemDelete()).build();

            case SYSTEMUNDELETE:
                return scheduleableTransactionBodyBuilder.setSystemUndelete(txBody.getSystemUndelete()).build();

            case FREEZE:
                return scheduleableTransactionBodyBuilder.setFreeze(txBody.getFreeze()).build();

            case CONSENSUSCREATETOPIC:
                return scheduleableTransactionBodyBuilder.setConsensusCreateTopic(txBody.getConsensusCreateTopic()).build();

            case CONSENSUSUPDATETOPIC:
                return scheduleableTransactionBodyBuilder.setConsensusUpdateTopic(txBody.getConsensusUpdateTopic()).build();

            case CONSENSUSDELETETOPIC:
                return scheduleableTransactionBodyBuilder.setConsensusDeleteTopic(txBody.getConsensusDeleteTopic()).build();

            case CONSENSUSSUBMITMESSAGE:
                return scheduleableTransactionBodyBuilder.setConsensusSubmitMessage(txBody.getConsensusSubmitMessage()).build();

            case TOKENASSOCIATE:
                return scheduleableTransactionBodyBuilder.setTokenAssociate(txBody.getTokenAssociate()).build();

            case TOKENBURN:
                return scheduleableTransactionBodyBuilder.setTokenBurn(txBody.getTokenBurn()).build();

            case TOKENCREATION:
                return scheduleableTransactionBodyBuilder.setTokenAssociate(txBody.getTokenAssociate()).build();

            case TOKENDELETION:
                return scheduleableTransactionBodyBuilder.setTokenDeletion(txBody.getTokenDeletion()).build();

            case TOKENDISSOCIATE:
                return scheduleableTransactionBodyBuilder.setTokenDissociate(txBody.getTokenDissociate()).build();

            case TOKENFREEZE:
                return scheduleableTransactionBodyBuilder.setTokenFreeze(txBody.getTokenFreeze()).build();

            case TOKENGRANTKYC:
                return scheduleableTransactionBodyBuilder.setTokenGrantKyc(txBody.getTokenGrantKyc()).build();

            case TOKENMINT:
                return scheduleableTransactionBodyBuilder.setTokenMint(txBody.getTokenMint()).build();

            case TOKENREVOKEKYC:
                return scheduleableTransactionBodyBuilder.setTokenRevokeKyc(txBody.getTokenRevokeKyc()).build();

            case TOKENUNFREEZE:
                return scheduleableTransactionBodyBuilder.setTokenUnfreeze(txBody.getTokenUnfreeze()).build();

            case TOKENUPDATE:
                return scheduleableTransactionBodyBuilder.setTokenUpdate(txBody.getTokenUpdate()).build();

            case TOKENWIPE:
                return scheduleableTransactionBodyBuilder.setTokenWipe(txBody.getTokenWipe()).build();

            case SCHEDULEDELETE:
                return scheduleableTransactionBodyBuilder.setScheduleDelete(txBody.getScheduleDelete()).build();

            default:
                throw new IllegalArgumentException("parsed transaction body has no data");
        }
    }
}
