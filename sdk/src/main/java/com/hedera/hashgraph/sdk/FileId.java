package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.FileID;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * The ID for a file on Hedera.
 */
public final class FileId {
    /**
     * The shard number
     */
    @Nonnegative
    public final long shard;

    /**
     * The realm number
     */
    @Nonnegative
    public final long realm;

    /**
     * The id number
     */
    @Nonnegative
    public final long num;

    @Nullable
    private final String checksum;

    /**
     * The public node address book for the current network.
     */
    public static final FileId ADDRESS_BOOK = new FileId(0, 0, 102);

    /**
     * The current fee schedule for the network.
     */
    public static final FileId FEE_SCHEDULE = new FileId(0, 0, 111);

    /**
     * The current exchange rate of HBAR to USD.
     */
    public static final FileId EXCHANGE_RATES = new FileId(0, 0, 112);

    public FileId(@Nonnegative long num) {
        this(0, 0, num);
    }

    @SuppressWarnings("InconsistentOverloads")
    public FileId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;
        this.checksum = null;
    }

    @SuppressWarnings("InconsistentOverloads")
    FileId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num, @Nullable NetworkName network, @Nullable String checksum) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;

        if (network != null) {
            if (checksum == null) {
                this.checksum = EntityIdHelper.checksum(Integer.toString(network.id), shard + "." + realm + "." + num);
            } else {
                this.checksum = checksum;
            }
        } else {
            this.checksum = null;
        }
    }

    public static FileId fromString(String id) {
        return EntityIdHelper.fromString(id, FileId::new);
    }

    public static FileId fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(FileID.parseFrom(bytes).toBuilder().build());
    }

    static FileId fromProtobuf(FileID fileId, @Nullable NetworkName networkName) {
        Objects.requireNonNull(fileId);
        return new FileId(fileId.getShardNum(), fileId.getRealmNum(), fileId.getFileNum(), networkName, null);
    }

    static FileId fromProtobuf(FileID fileId) {
        return FileId.fromProtobuf(fileId, null);
    }

    FileID toProtobuf() {
        return FileID.newBuilder()
            .setShardNum(shard)
            .setRealmNum(realm)
            .setFileNum(num)
            .build();
    }

    @Deprecated
    public void validate(Client client) {
        validateChecksum(client);
    }

    public void validateChecksum(Client client) {
        EntityIdHelper.validate(shard, realm, num, client, checksum);
    }

    @Nullable
    public String getChecksum() {
        return checksum;
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }

    @Override
    public String toString() {
        return EntityIdHelper.toString(shard, realm, num);
    }

    public String toStringWithChecksum(Client client) {
        return EntityIdHelper.toStringWithChecksum(shard, realm, num, client, checksum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shard, realm, num);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileId)) return false;

        FileId otherId = (FileId) o;
        return shard == otherId.shard && realm == otherId.realm && num == otherId.num;
    }
}
