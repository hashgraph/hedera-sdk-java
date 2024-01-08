package com.hedera.hashgraph.sdk;

import static com.hedera.hashgraph.sdk.MirrorNodeRouter.ACCOUNTS_ROUTE;
import static com.hedera.hashgraph.sdk.MirrorNodeRouter.ACCOUNT_TOKENS_ROUTE;
import static com.hedera.hashgraph.sdk.MirrorNodeRouter.CONTRACTS_ROUTE;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

class MirrorNodeGateway {

    private final String mirrorNodeUrl;

    static final Duration MIRROR_NODE_CONNECTION_TIMEOUT = Duration.ofSeconds(30);

    private MirrorNodeGateway(String mirrorNodeUrl) {
        this.mirrorNodeUrl = mirrorNodeUrl;
    }

    static MirrorNodeGateway forClient(Client client) {
        final String mirrorNodeUrl = MirrorNodeRouter.getMirrorNodeUrl(client.getMirrorNetwork(),
            client.getLedgerId());

        return new MirrorNodeGateway(mirrorNodeUrl);
    }

    static MirrorNodeGateway forNetwork(List<String> mirrorNetwork, LedgerId ledgerId) {
        final String mirrorNodeUrl = MirrorNodeRouter.getMirrorNodeUrl(mirrorNetwork, ledgerId);

        return new MirrorNodeGateway(mirrorNodeUrl);
    }

    JsonObject getAccountInfo(String idOrAliasOrEvmAddress) throws IOException, InterruptedException {
        var fullApiUrl = MirrorNodeRouter.buildApiUrl(mirrorNodeUrl, ACCOUNTS_ROUTE, idOrAliasOrEvmAddress);

        var responseBody = performQueryToMirrorNode(fullApiUrl);

        return JsonParser.parseString(responseBody).getAsJsonObject();
    }

    JsonObject getContractInfo(String contractIdOrAddress) throws IOException, InterruptedException {
        var fullApiUrl = MirrorNodeRouter.buildApiUrl(mirrorNodeUrl, CONTRACTS_ROUTE, contractIdOrAddress);

        var responseBody = performQueryToMirrorNode(fullApiUrl);

        return JsonParser.parseString(responseBody).getAsJsonObject();
    }

    JsonObject getAccountTokens(String idOrAliasOrEvmAddress) throws IOException, InterruptedException {
        var fullApiUrl = MirrorNodeRouter.buildApiUrl(mirrorNodeUrl, ACCOUNT_TOKENS_ROUTE, idOrAliasOrEvmAddress);

        var responseBody = performQueryToMirrorNode(fullApiUrl);

        return JsonParser.parseString(responseBody).getAsJsonObject();
    }

    private String performQueryToMirrorNode(String apiUrl) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .timeout(MIRROR_NODE_CONNECTION_TIMEOUT)
            .uri(URI.create(apiUrl))
            .build();

        return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body();
    }
}
