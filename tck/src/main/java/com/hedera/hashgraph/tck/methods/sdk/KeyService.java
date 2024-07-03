package com.hedera.hashgraph.tck.methods.sdk;

import static com.hedera.hashgraph.tck.util.KeyUtils.KeyType.*;
import static com.hedera.hashgraph.tck.util.KeyUtils.getKeyFromStringDER;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.Key;
import com.hedera.hashgraph.sdk.KeyList;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.tck.annotation.JSONRPC2Method;
import com.hedera.hashgraph.tck.annotation.JSONRPC2Service;
import com.hedera.hashgraph.tck.exception.InvalidJSONRPC2ParamsException;
import com.hedera.hashgraph.tck.exception.InvalidJSONRPC2RequestException;
import com.hedera.hashgraph.tck.methods.AbstractJSONRPC2Service;
import com.hedera.hashgraph.tck.methods.sdk.param.GenerateKeyParams;
import com.hedera.hashgraph.tck.methods.sdk.response.GenerateKeyResponse;
import org.bouncycastle.util.encoders.Hex;

@JSONRPC2Service
public class KeyService extends AbstractJSONRPC2Service {

    @JSONRPC2Method("generateKey")
    public GenerateKeyResponse generateKey(final GenerateKeyParams params)
            throws InvalidJSONRPC2RequestException, InvalidJSONRPC2ParamsException, InvalidProtocolBufferException {
        GenerateKeyResponse response = new GenerateKeyResponse();
        response.setKey(recursionSwitch(params, response, false));
        return response;
    }

    private String recursionSwitch(final GenerateKeyParams params, final GenerateKeyResponse response, boolean isList)
            throws InvalidJSONRPC2ParamsException, InvalidJSONRPC2RequestException, InvalidProtocolBufferException {
        switch (params.getType()) {
            case ED25519_PRIVATE_KEY, ECDSA_SECP256K1_PRIVATE_KEY:
                var privateKey = params.getType().equals(ED25519_PRIVATE_KEY)
                        ? PrivateKey.generateED25519().toStringDER()
                        : PrivateKey.generateECDSA().toStringDER();
                if (isList) {
                    response.getPrivateKeys().add(privateKey);
                }

                return privateKey;

            case ED25519_PUBLIC_KEY, ECDSA_SECP256K1_PUBLIC_KEY:
                if (params.getFromKey().isPresent()) {
                    return PrivateKey.fromString(params.getFromKey().get())
                            .getPublicKey()
                            .toStringDER();
                }
                var publicKey = params.getType().equals(ED25519_PUBLIC_KEY)
                        ? PrivateKey.generateED25519().toStringDER()
                        : PrivateKey.generateECDSA().toStringDER();
                if (isList) {
                    response.getPrivateKeys().add(publicKey);
                }

                return publicKey;

            case LIST_KEY, THRESHOLD_KEY:
                KeyList keyList = new KeyList();
                params.getKeys().get().forEach(keyParams -> {
                    try {
                        keyList.add(getKeyFromStringDER(recursionSwitch(keyParams, response, true)));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                if (params.getType().equals(THRESHOLD_KEY)) {
                    keyList.setThreshold(params.getThreshold().get().intValue());
                }

                // TODO this is not public
                return Hex.toHexString(keyList.toProtobufKey().toByteArray());

            case EVM_ADDRESS_KEY:
                if (params.getFromKey().isPresent()) {
                    Key hederaKey = getKeyFromStringDER(params.getFromKey().get());
                    if (hederaKey instanceof PrivateKey pk) {
                        return pk.getPublicKey().toEvmAddress().toString();
                    } else if (hederaKey instanceof PublicKey pk) {
                        return pk.toEvmAddress().toString();
                    } else {
                        throw new InvalidJSONRPC2ParamsException(
                                "invalid parameters: fromKey for evmAddress is not ECDSAsecp256k1.");
                    }
                }
                return PrivateKey.generateECDSA().getPublicKey().toEvmAddress().toString();

            default:
                throw new InvalidJSONRPC2RequestException("invalid request: key type not recognized.");
        }
    }
}
