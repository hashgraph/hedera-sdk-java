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

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.NftId;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenId;
import com.hedera.hashgraph.sdk.TokenMintTransaction;
import com.hedera.hashgraph.sdk.TokenNftInfoQuery;
import com.hedera.hashgraph.sdk.TokenType;
import com.hedera.hashgraph.sdk.TokenUpdateNftsTransaction;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UpdateNftsExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    public UpdateNftsExample() {
    }

    public static void main(String[] args) throws Exception {
        Client client = ClientHelper.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        var metadataKey = PrivateKey.generateED25519();
        var nftCount = 4;
        var initialMetadataList = generateMetadata(new byte[]{4, 2, 0}, nftCount);

        // create a token with metadata key
        var tokenId = Objects.requireNonNull(
            new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
                .setTreasuryAccountId(OPERATOR_ID)
                .setAdminKey(OPERATOR_KEY)
                .setSupplyKey(OPERATOR_KEY)
                .setMetadataKey(metadataKey)
                .execute(client)
                .getReceipt(client)
                .tokenId
        );

        // mint tokens
        var nftSerials = new TokenMintTransaction()
            .setMetadata(initialMetadataList)
            .setTokenId(tokenId)
            .execute(client)
            .getReceipt(client)
            .serials;

        // check that metadata was set correctly
        System.out.println("Metadata after mint= " + getMetadataList(client, tokenId, nftSerials));

        // update metadata of the first two minted NFTs
        var updatedMetadata = new byte[]{6, 9};
        nftSerials = new TokenUpdateNftsTransaction()
            .setTokenId(tokenId)
            .setSerials(nftSerials.subList(0, nftCount / 2))
            .setMetadata(updatedMetadata)
            .sign(metadataKey)
            .execute(client)
            .getReceipt(client)
            .serials;

        // check that metadata was updated correctly
        System.out.println("Metadata after mint= " + getMetadataList(client, tokenId, nftSerials));

        client.close();
    }

    private static List<byte[]> generateMetadata(byte[] metadata, int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> metadata.clone())
            .collect(Collectors.toList());
    }

    private static List<byte[]> getMetadataList(Client client, TokenId tokenId, List<Long> nftSerials) {
        return nftSerials.stream()
            .map(serial -> new NftId(tokenId, serial))
            .flatMap(nftId -> {
                try {
                    return new TokenNftInfoQuery()
                        .setNftId(nftId)
                        .execute(client).stream();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            })
            .map(tokenNftInfo -> tokenNftInfo.metadata)
            .toList();
    }
}
