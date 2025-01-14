// SPDX-License-Identifier: Apache-2.0
package org.hiero.tck.methods.sdk.param;

import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minidev.json.JSONObject;
import org.hiero.tck.methods.JSONRPC2Param;

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
        private String amount;
        private Optional<String> denominatingTokenId;

        public static FixedFee parse(Map<String, Object> jrpcParams) throws Exception {
            var amountParsed = (String) jrpcParams.get("amount");
            var denominatingTokenIdParsed = Optional.ofNullable((String) jrpcParams.get("denominatingTokenId"));
            return new FixedFee(amountParsed, denominatingTokenIdParsed);
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FractionalFee {
        private String numerator;
        private String denominator;
        private String minimumAmount;
        private String maximumAmount;

        public static FractionalFee parse(Map<String, Object> jrpcParams) throws Exception {
            var numeratorParsed = (String) jrpcParams.get("numerator");
            var denominatorParsed = (String) jrpcParams.get("denominator");
            var minimumAmountParsed = (String) jrpcParams.get("minimumAmount");
            var maximumAmountParsed = (String) jrpcParams.get("maximumAmount");
            return new FractionalFee(numeratorParsed, denominatorParsed, minimumAmountParsed, maximumAmountParsed);
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoyaltyFee {
        private String numerator;
        private String denominator;
        private Optional<FixedFee> fallbackFee;

        public static RoyaltyFee parse(Map<String, Object> jrpcParams) throws Exception {
            var numeratorParsed = (String) jrpcParams.get("numerator");
            var denominatorParsed = (String) jrpcParams.get("denominator");

            Optional<FixedFee> fallbackFeeParsed = Optional.empty();
            if (jrpcParams.containsKey("fallbackFee")) {
                JSONObject jsonObject = (JSONObject) jrpcParams.get("fallbackFee");
                fallbackFeeParsed = Optional.of(FixedFee.parse(jsonObject));
            }

            return new RoyaltyFee(numeratorParsed, denominatorParsed, fallbackFeeParsed);
        }
    }
}
