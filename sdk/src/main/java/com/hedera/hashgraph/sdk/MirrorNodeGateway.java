package com.hedera.hashgraph.sdk;

import static com.hedera.hashgraph.sdk.MirrorNodeRouter.ACCOUNTS_ROUTE;
import static com.hedera.hashgraph.sdk.MirrorNodeRouter.ACCOUNT_TOKENS_ROUTE;

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

    static MirrorNodeGateway forNetwork(List<String> mirrorNetwork, LedgerId ledgerId) {
        final String mirrorNodeUrl = MirrorNodeRouter.getMirrorNodeUrl(mirrorNetwork, ledgerId);

        return new MirrorNodeGateway(mirrorNodeUrl);
    }

    JsonObject getAccountInfo(String id) throws IOException, InterruptedException {
        var fullApiUrl = MirrorNodeRouter.buildApiUrl(mirrorNodeUrl, ACCOUNTS_ROUTE, id);

        var responseBody = performQueryToMirrorNode(fullApiUrl);

        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
        return jsonObject;
    }

    JsonObject getAccountTokens(String id) throws IOException, InterruptedException {
        var fullApiUrl = MirrorNodeRouter.buildApiUrl(mirrorNodeUrl, ACCOUNT_TOKENS_ROUTE, id);

        var responseBody = performQueryToMirrorNode(fullApiUrl);

        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
        return jsonObject;
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
