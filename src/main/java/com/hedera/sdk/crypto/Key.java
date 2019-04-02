package com.hedera.sdk.crypto;

import com.hedera.sdk.ContractId;
import com.hedera.sdk.crypto.ed25519.Ed25519PublicKey;
import com.hedera.sdk.proto.ContractID;

public interface Key {
    com.hedera.sdk.proto.Key toKeyProto();

    static Key fromProtoKey(com.hedera.sdk.proto.Key key) {
        switch (key.getKeyCase()) {
        case ED25519:
            return Ed25519PublicKey.fromBytes(
                key.getEd25519()
                    .toByteArray()
            );
        case CONTRACTID:
            ContractID id = key.getContractID();
            return new ContractId(id.getShardNum(), id.getRealmNum(), id.getContractNum());
        default:
            throw new IllegalStateException("Unchecked Key Case");
        }
    }
}
