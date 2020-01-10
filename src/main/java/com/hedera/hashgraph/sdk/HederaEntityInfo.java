package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.proto.GetBySolidityIDResponse;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.contract.ContractId;
import com.hedera.hashgraph.sdk.file.FileId;

import javax.annotation.Nullable;

/**
 * Returned by {@link GetBySolidityIdQuery}.
 */
public final class HederaEntityInfo {

    /**
     * The type of the entity returned.
     */
    public final Type type;

    /**
     * The ID of the account corresponding to the given Solidity ID, or the cryptocurrency
     * account associated with a contract where {@link #contractId} corresponds to the given
     * Solidity ID.
     *
     * Set if {@code type == Account} or {@code type == Contract}.
     */
    @Nullable
    public final AccountId accountId;

    /**
     * The ID of the contract corresponding to the given Solidity ID.
     *
     * If this is set then {@link #accountId} is the cryptocurrency account associated with
     * this contract.
     *
     * Set if {@code type == Contract}.
     */
    @Nullable
    public final ContractId contractId;

    /**
     * The ID of the file corresponding to the given Solidity ID.
     *
     * Set if {@code type == File}.
     */
    @Nullable
    public final FileId fileId;

    HederaEntityInfo(GetBySolidityIDResponse inner) {
        this.accountId = inner.hasAccountID() ? new AccountId(inner.getAccountID()) : null;
        this.contractId = inner.hasContractID() ? new ContractId(inner.getContractID()) : null;
        this.fileId = inner.hasFileID() ? new FileId(inner.getFileID()) : null;

        if (inner.hasContractID() && inner.hasAccountID()) {
            this.type = Type.Contract;
        } else if (inner.hasAccountID()) {
            this.type = Type.Account;
        } else if (inner.hasFileID()) {
            this.type = Type.File;
        } else {
            this.type = Type.Unknown;
        }
    }

    public enum Type {
        Account,
        Contract,
        File,
        Unknown
    }
}
