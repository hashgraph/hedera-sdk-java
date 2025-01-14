// SPDX-License-Identifier: Apache-2.0
package org.hiero.tck.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.hiero.tck.exception.JSONRPCParseException;
import org.hiero.tck.methods.sdk.param.CommonTransactionParams;
import org.hiero.tck.methods.sdk.param.CustomFee;

public class JSONRPCParamParser {

    public static Optional<CommonTransactionParams> parseCommonTransactionParams(Map<String, Object> jrpcParams) {
        try {
            return parseJsonObject(jrpcParams, "commonTransactionParams", CommonTransactionParams::parse);
        } catch (Exception e) {
            throw new JSONRPCParseException("Failed to parse CommonTransactionParams", e);
        }
    }

    public static Optional<List<CustomFee>> parseCustomFees(Map<String, Object> jrpcParams) {
        try {
            return parseJsonArray(jrpcParams, "customFees", jsonObj -> {
                try {
                    return new CustomFee().parse(jsonObj);
                } catch (Exception e) {
                    throw new JSONRPCParseException("Failed to parse CustomFee", e);
                }
            });
        } catch (Exception e) {
            throw new JSONRPCParseException("Failed to parse CustomFees array", e);
        }
    }

    private static <T> Optional<T> parseJsonObject(
            Map<String, Object> params, String key, ThrowingFunction<JSONObject, T> parser) throws Exception {
        if (!params.containsKey(key)) {
            return Optional.empty();
        }
        JSONObject jsonObject = (JSONObject) params.get(key);
        return Optional.of(parser.apply(jsonObject));
    }

    private static <T> Optional<List<T>> parseJsonArray(
            Map<String, Object> params, String key, ThrowingFunction<JSONObject, T> elementParser) throws Exception {
        if (!params.containsKey(key)) {
            return Optional.empty();
        }
        JSONArray jsonArray = (JSONArray) params.get(key);
        List<T> results = new ArrayList<>();

        for (Object o : jsonArray) {
            JSONObject jsonObject = (JSONObject) o;
            results.add(elementParser.apply(jsonObject));
        }
        return Optional.of(results);
    }

    @FunctionalInterface
    private interface ThrowingFunction<T, R> {
        R apply(T t) throws Exception;
    }
}
