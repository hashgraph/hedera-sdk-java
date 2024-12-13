/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2024 Hedera Hashgraph, LLC
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

import java.util.concurrent.ExecutionException;

public class MirrorNodeContractCallQuery extends MirrorNodeContractQuery<MirrorNodeContractCallQuery> {
    /**
     * Does transient simulation of read-write operations and returns the result in hexadecimal string format.
     *
     * @param client The Client instance to perform the operation with
     * @return The result of the contract call
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public String execute(Client client) throws ExecutionException, InterruptedException {
        return call(client);
    }

    @Override
    public String toString() {
        return "MirrorNodeContractCallQuery" + super.toString();
    }
}