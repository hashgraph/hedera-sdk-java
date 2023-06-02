package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.HederaFunctionality;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class RequestTypeTest {

    @Test
    void valueOf() {
        var codeValues = HederaFunctionality.values();
        var requestTypeValues = RequestType.values();
        var pair = IntStream.range(0, codeValues.length-1)
            .mapToObj(i -> Map.entry(codeValues[i], requestTypeValues[i]))
            .collect(Collectors.toList());

        pair.forEach(a -> {
            var code = a.getKey();
            var requestType = a.getValue();
            assertThat(RequestType.valueOf(code)).hasToString(requestType.toString());
        });
    }
}
