package com.hedera.sdk;

import com.hedera.sdk.proto.EntityID;

public final class Entity {

    private final EntityID.Builder inner;

    public Entity(EntityID proto) {
        inner = proto.toBuilder();
    }

    public AccountId getAccount() throws InvalidEntityRequestException {
        if (!inner.hasAccountID()) {
            throw new InvalidEntityRequestException("Does not contain AccountId");
        }

        var raw = inner.getAccountID();

        return new AccountId(raw.getShardNum(), raw.getRealmNum(), raw.getAccountNum());
    }

    // todo: Claim

    public FileId getFile() throws InvalidEntityRequestException {
        if (!inner.hasFileID()) {
            throw new InvalidEntityRequestException("Does not contain FileId");
        }

        var raw = inner.getFileID();

        return new FileId(raw.getShardNum(), raw.getRealmNum(), raw.getFileNum());
    }

    public ContractId getContract() throws InvalidEntityRequestException {
        if (!inner.hasContractID()) {
            throw new InvalidEntityRequestException("Does not contain ContractId");
        }

        var raw = inner.getContractID();

        return new ContractId(raw.getShardNum(), raw.getRealmNum(), raw.getContractNum());
    }
}
