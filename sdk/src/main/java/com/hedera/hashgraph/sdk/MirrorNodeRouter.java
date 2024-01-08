package com.hedera.hashgraph.sdk;

import java.util.List;
import java.util.Map;
import java.util.Optional;

class MirrorNodeRouter {

    private static final String API_VERSION = "/api/v1";

    // TODO: check how to make it configurable, not hardcoded
    private static final String LOCAL_NODE_PORT_NUMBER = "5551";

    static final String ACCOUNTS_ROUTE = "accounts";

    static final String CONTRACTS_ROUTE = "contracts";

    static final String ACCOUNT_TOKENS_ROUTE = "account_tokens";

    private static final Map<String, String> routes = Map.of(
        ACCOUNTS_ROUTE, "/accounts/%s",
        CONTRACTS_ROUTE, "/contracts/%s",
        ACCOUNT_TOKENS_ROUTE, "/accounts/%s/tokens"
    );

    static String getMirrorNodeUrl(List<String> mirrorNetwork, LedgerId ledgerId) {
        Optional<String> mirrorNodeAddress = mirrorNetwork.stream()
            .map(address -> address.substring(0, address.indexOf(":")))
            .findFirst();

        if (mirrorNodeAddress.isEmpty()) {
            throw new IllegalArgumentException("Mirror address not found");
        }

        String fullMirrorNodeUrl;

        if (ledgerId != null) {
            fullMirrorNodeUrl = "https://" + mirrorNodeAddress.get();
        } else {
            // local node case
            fullMirrorNodeUrl = "http://" + mirrorNodeAddress.get() + ":" + LOCAL_NODE_PORT_NUMBER;
        }

        return fullMirrorNodeUrl;
    }

    static String buildApiUrl(String mirrorNodeUrl, String route, String id) {
        return mirrorNodeUrl + API_VERSION + String.format(routes.get(route), id);
    }
}
