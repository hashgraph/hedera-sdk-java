package com.hedera.hashgraph.sdk.file;

import com.hedera.hashgraph.proto.FileID;
import com.hedera.hashgraph.proto.FileIDOrBuilder;
import com.hedera.hashgraph.sdk.IdUtil;
import com.hedera.hashgraph.sdk.Internal;
import com.hedera.hashgraph.sdk.SolidityUtil;

import java.util.Objects;

public final class FileId {
    public final long shard;
    public final long realm;
    public final long file;

    /**
     * The public node address book for the current network.
     *
     * This file can be decoded using {@link com.hedera.hashgraph.proto.NodeAddressBook}.
     */
    public static final FileId ADDRESS_BOOK = new FileId(0, 0, 102);

    /**
     * The current fee schedule for the network.
     *
     * This file can be decoded using {@link com.hedera.hashgraph.proto.CurrentAndNextFeeSchedule}.
     */
    public static final FileId FEE_SCHEDULE = new FileId(0, 0, 111);

    /**
     * The current exchange rate of HBAR to USD.
     *
     * This file can be decoded using {@link com.hedera.hashgraph.proto.ExchangeRateSet}.
     */
    public static final FileId EXCHANGE_RATES = new FileId(0, 0, 112);

    public FileId(long shardNum, long realmNum, long fileNum) {
        this.shard = shardNum;
        this.realm = realmNum;
        this.file = fileNum;
    }

    public FileId(FileIDOrBuilder fileId) {
        this(fileId.getShardNum(), fileId.getRealmNum(), fileId.getFileNum());
    }

    /** Constructs a `FileId` from a string formatted as <shardNum>.<realmNum>.<fileNum> */
    public static FileId fromString(String account) throws IllegalArgumentException {
        return IdUtil.parseIdString(account, FileId::new);
    }

    /**
     * @deprecated the Solidity VM has no concept of files, so this method has limited utility.
     */
    @Deprecated
    public static FileId fromSolidityAddress(String address) {
        return SolidityUtil.parseAddress(address, FileId::new);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shard, realm, file);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;

        if (!(other instanceof FileId)) return false;

        FileId otherId = (FileId) other;
        return shard == otherId.shard && realm == otherId.realm && file == otherId.file;
    }

    @Internal
    public FileID toProto() {
        return FileID.newBuilder()
            .setShardNum(shard)
            .setRealmNum(realm)
            .setFileNum(file)
            .build();
    }

    @Override
    public String toString() {
        return "" + shard + "." + realm + "." + file;
    }

    /**
     * @deprecated the Solidity VM has no concept of files, so this method has limited utility.
     */
    @Deprecated
    public String toSolidityAddress() {
        return SolidityUtil.addressFor(this);
    }
}
