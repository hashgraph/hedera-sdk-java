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
package com.hedera.hashgraph.sdk;

import java.util.List;
import java.util.Map;
import java.util.Optional;

class MirrorNodeRouter {

    private static final String API_VERSION = "/api/v1";

    // refactor me
    static String LOCAL_NODE_PORT = "5551";

    static final String ACCOUNTS_ROUTE = "accounts";

    static final String CONTRACTS_ROUTE = "contracts";

    static final String ACCOUNT_TOKENS_ROUTE = "account_tokens";

    private static final Map<String, String> routes = Map.of(
        ACCOUNTS_ROUTE, "/accounts/%s",
        CONTRACTS_ROUTE, "/contracts/%s",
        ACCOUNT_TOKENS_ROUTE, "/accounts/%s/tokens"
    );

    private MirrorNodeRouter() {
    }

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
            fullMirrorNodeUrl = "http://" + mirrorNodeAddress.get() + ":" + LOCAL_NODE_PORT;
        }

        return fullMirrorNodeUrl;
    }

    static String buildApiUrl(String mirrorNodeUrl, String route, String id) {
        return mirrorNodeUrl + API_VERSION + String.format(routes.get(route), id);
    }
}
