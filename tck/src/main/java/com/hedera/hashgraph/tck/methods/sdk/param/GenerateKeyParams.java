package com.hedera.hashgraph.tck.methods.sdk.param;

import static com.hedera.hashgraph.tck.util.KeyUtils.KeyType.ECDSA_SECP256K1_PUBLIC_KEY;
import static com.hedera.hashgraph.tck.util.KeyUtils.KeyType.ED25519_PUBLIC_KEY;
import static com.hedera.hashgraph.tck.util.KeyUtils.KeyType.EVM_ADDRESS_KEY;
import static com.hedera.hashgraph.tck.util.KeyUtils.KeyType.LIST_KEY;
import static com.hedera.hashgraph.tck.util.KeyUtils.KeyType.THRESHOLD_KEY;

import com.hedera.hashgraph.tck.exception.InvalidJSONRPC2RequestException;
import com.hedera.hashgraph.tck.methods.JSONRPC2Param;
import com.hedera.hashgraph.tck.util.KeyUtils.KeyType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GenerateKeyParams extends JSONRPC2Param {
    private KeyType type;
    private Optional<String> fromKey;
    private Optional<Long> threshold;
    private Optional<List<GenerateKeyParams>> keys;

    @Override
    public GenerateKeyParams parse(Map<String, Object> jrpcParams) throws Exception {
        var type = (String) jrpcParams.get("type");
        var fromKey = Optional.ofNullable((String) jrpcParams.get("fromKey"));
        var threshold = Optional.ofNullable((Long) jrpcParams.get("threshold"));

        Optional<List<GenerateKeyParams>> keys = Optional.empty();
        if (jrpcParams.containsKey("keys")) {
            JSONArray jsonArray = (JSONArray) jrpcParams.get("keys");
            List<GenerateKeyParams> keyList = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                GenerateKeyParams keyParam = new GenerateKeyParams().parse(jsonObject);
                keyList.add(keyParam);
            }
            keys = Optional.of(keyList);
        }

        // Make sure getFromKey() is only provided for ED25519_PUBLIC_KEY, ECDSA_SECP256k1_PUBLIC_KEY, or
        // EVM_ADDRESS_KEY
        if (fromKey.isPresent()
                && !type.equals(ED25519_PUBLIC_KEY.getKeyString())
                && !type.equals(ECDSA_SECP256K1_PUBLIC_KEY.getKeyString())
                && !type.equals(EVM_ADDRESS_KEY.getKeyString())) {
            throw new IllegalArgumentException(
                    "invalid parameters: fromKey should only be provided for ed25519PublicKey, ecdsaSecp256k1PublicKey, or evmAddress types.");
        }

        // Make sure threshold is only provided for THRESHOLD_KEY_TYPE.
        if (threshold.isPresent() && !type.equals(THRESHOLD_KEY.getKeyString())) {
            throw new IllegalArgumentException(
                    "invalid parameters: threshold should only be provided for thresholdKey types.");
        }

        // Make sure keys is only provided for LIST_KEY_TYPE or THRESHOLD_KEY_TYPE
        if (keys.isPresent() && !type.equals(LIST_KEY.getKeyString()) && !type.equals(THRESHOLD_KEY.getKeyString())) {
            throw new IllegalArgumentException(
                    "invalid parameters: keys should only be provided for keyList or thresholdKey types.");
        }

        if ((type.equals(THRESHOLD_KEY.getKeyString()) || type.equals(LIST_KEY.getKeyString())) && keys.isEmpty()) {
            throw new InvalidJSONRPC2RequestException(
                    "invalid request: keys list is required for generating a KeyList type.");
        }

        if (type.equals(THRESHOLD_KEY.getKeyString()) && threshold.isEmpty()) {
            throw new InvalidJSONRPC2RequestException(
                    "invalid request: threshold is required for generating a ThresholdKey type.");
        }

        return new GenerateKeyParams(KeyType.fromString(type), fromKey, threshold, keys);
    }
}
