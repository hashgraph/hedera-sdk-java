/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2023 - 2024 Hedera Hashgraph, LLC
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
package com.hedera.hashgraph.sdk.examples;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;

/**
 * In Hedera there are 4 different account representations:
 *  - an account can have an account ID in shard.realm.accountNumber format (0.0.10)
 *  - an account can have a public key alias in 0.0.CIQNOWUYAGBLCCVX2VF75U6JMQDTUDXBOLZ5VJRDEWXQEGTI64DVCGQ format
 *  - an account can have an AccountId that is represented in 0x000000000000000000000000000000000000000a (for account ID 0.0.10) long zero format
 *  - an account can be represented by an Ethereum public address 0xb794f5ea0ba39494ce839613fffba74279579268
 *
 * Reference: HIP-583 Expand alias support in CryptoCreate & CryptoTransfer Transactions | https://hips.hedera.com/hip/hip-583
 */
class AccountCreationWaysExample {

    public static void main(String[] args) {
        System.out.println("Account Creation Ways Example Start!");

        /*
         * Account ID -- shard.realm.number format, i.e. `0.0.10` with the corresponding `0x000000000000000000000000000000000000000a` ethereum address.
         */
        AccountId hederaFormat = AccountId.fromString("0.0.10");
        System.out.println("Account ID: " + hederaFormat);
        System.out.println("Account \"0.0.10\" corresponding long-zero address: " + hederaFormat.toSolidityAddress());

        /*
         * Hedera Long-Form Account ID -- 0.0.aliasPublicKey, i.e. `0.0.CIQNOWUYAGBLCCVX2VF75U6JMQDTUDXBOLZ5VJRDEWXQEGTI64DVCGQ`.
         */
        PrivateKey privateKey = PrivateKey.generateECDSA();
        PublicKey publicKey = privateKey.getPublicKey();

        // Assuming that the target shard and realm are known.
        // For now they are virtually always 0.
        AccountId aliasAccountId = publicKey.toAccountId(0, 0);
        System.out.println("Hedera long-form account ID: " + aliasAccountId.toString());

        /*
         * Hedera Account Long-Zero address -- 0x000000000000000000000000000000000000000a (for accountId 0.0.10).
         */
        AccountId longZeroAddress = AccountId.fromString("0x000000000000000000000000000000000000000a");
        System.out.println("Hedera account long-zero address: " + longZeroAddress);

        /*
         * Ethereum Account Address / public-address -- 0xb794f5ea0ba39494ce839613fffba74279579268.
         */
        AccountId evmAddress = AccountId.fromString("0xb794f5ea0ba39494ce839613fffba74279579268");
        System.out.println("Ethereum account address / public address: " + evmAddress);

        System.out.println("Account Creation Ways Example Complete!");
    }
}
