/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2024 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.hedera.hashgraph.tck.methods.sdk.param;

import com.hedera.hashgraph.tck.methods.JSONRPC2Param;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minidev.json.JSONObject;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CustomFee extends JSONRPC2Param {

    private String feeCollectorAccountId;
    private Boolean feeCollectorsExempt;
    private Optional<FixedFee> fixedFee;
    private Optional<FractionalFee> fractionalFee;
    private Optional<RoyaltyFee> royaltyFee;

    @Override
    public CustomFee parse(Map<String, Object> jrpcParams) throws Exception {
        var feeCollectorAccountIdParsed = (String) jrpcParams.get("feeCollectorAccountId");
        var feeCollectorsExemptParsed = (Boolean) jrpcParams.get("feeCollectorsExempt");

        Optional<FixedFee> fixedFeeParsed = Optional.empty();
        if (jrpcParams.containsKey("fixedFee")) {
            JSONObject jsonObject = (JSONObject) jrpcParams.get("fixedFee");
            fixedFeeParsed = Optional.of(FixedFee.parse(jsonObject));
        }

        Optional<FractionalFee> fractionalFeeParsed = Optional.empty();
        if (jrpcParams.containsKey("fractionalFee")) {
            JSONObject jsonObject = (JSONObject) jrpcParams.get("fractionalFee");
            fractionalFeeParsed = Optional.of(FractionalFee.parse(jsonObject));
        }

        Optional<RoyaltyFee> royaltyFeeParsed = Optional.empty();
        if (jrpcParams.containsKey("royaltyFee")) {
            JSONObject jsonObject = (JSONObject) jrpcParams.get("royaltyFee");
            royaltyFeeParsed = Optional.of(RoyaltyFee.parse(jsonObject));
        }

        return new CustomFee(
                feeCollectorAccountIdParsed,
                feeCollectorsExemptParsed,
                fixedFeeParsed,
                fractionalFeeParsed,
                royaltyFeeParsed);
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FixedFee {
        private Long amount;
        private Optional<String> denominatingTokenId;

        public static FixedFee parse(Map<String, Object> jrpcParams) throws Exception {
            var amountParsed = (Long) jrpcParams.get("amount");
            var denominatingTokenIdParsed = Optional.ofNullable((String) jrpcParams.get("denominatingTokenId"));
            return new FixedFee(amountParsed, denominatingTokenIdParsed);
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FractionalFee {
        private Long numerator;
        private Long denominator;
        private Long minimumAmount;
        private Long maximumAmount;

        public static FractionalFee parse(Map<String, Object> jrpcParams) throws Exception {
            var numeratorParsed = (Long) jrpcParams.get("numerator");
            var denominatorParsed = (Long) jrpcParams.get("denominator");
            var minimumAmountParsed = (Long) jrpcParams.get("minimumAmount");
            var maximumAmountParsed = (Long) jrpcParams.get("maximumAmount");
            return new FractionalFee(numeratorParsed, denominatorParsed, minimumAmountParsed, maximumAmountParsed);
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoyaltyFee {
        private Long numerator;
        private Long denominator;
        private Optional<FixedFee> fallbackFee;

        public static RoyaltyFee parse(Map<String, Object> jrpcParams) throws Exception {
            var numeratorParsed = (Long) jrpcParams.get("numerator");
            var denominatorParsed = (Long) jrpcParams.get("denominator");

            Optional<FixedFee> fallbackFeeParsed = Optional.empty();
            if (jrpcParams.containsKey("fallbackFee")) {
                JSONObject jsonObject = (JSONObject) jrpcParams.get("fallbackFee");
                fallbackFeeParsed = Optional.of(FixedFee.parse(jsonObject));
            }

            return new RoyaltyFee(numeratorParsed, denominatorParsed, fallbackFeeParsed);
        }
    }
}
