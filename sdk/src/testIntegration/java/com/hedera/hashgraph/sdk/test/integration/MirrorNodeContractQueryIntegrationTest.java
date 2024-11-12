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

package com.hedera.hashgraph.sdk.test.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.hedera.hashgraph.sdk.ContractCallQuery;
import com.hedera.hashgraph.sdk.ContractCreateTransaction;
import com.hedera.hashgraph.sdk.ContractDeleteTransaction;
import com.hedera.hashgraph.sdk.ContractId;
import com.hedera.hashgraph.sdk.FileCreateTransaction;
import com.hedera.hashgraph.sdk.FileDeleteTransaction;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.MirrorNodeContractQuery;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MirrorNodeContractQueryIntegrationTest {
    private static final String SMART_CONTRACT_BYTECODE = "6080604052348015600e575f80fd5b50335f806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506102228061005b5f395ff3fe608060405234801561000f575f80fd5b5060043610610034575f3560e01c80637065cb4814610038578063893d20e814610054575b5f80fd5b610052600480360381019061004d9190610199565b610072565b005b61005c610114565b60405161006991906101d3565b60405180910390f35b805f806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550600181908060018154018082558091505060019003905f5260205f20015f9091909190916101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555050565b5f805f9054906101000a900473ffffffffffffffffffffffffffffffffffffffff16905090565b5f80fd5b5f73ffffffffffffffffffffffffffffffffffffffff82169050919050565b5f6101688261013f565b9050919050565b6101788161015e565b8114610182575f80fd5b50565b5f813590506101938161016f565b92915050565b5f602082840312156101ae576101ad61013b565b5b5f6101bb84828501610185565b91505092915050565b6101cd8161015e565b82525050565b5f6020820190506101e65f8301846101c4565b9291505056fea2646970667358221220a02fed47a387783e8f429664c5f72014d9e3c1a31c5a9c197090b10fc5ea96f864736f6c634300081a0033";

    @Test
    @DisplayName("Can estimate and simulate transaction")
    void canSimulateTransaction() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {
            var response = new FileCreateTransaction()
                .setKeys(testEnv.operatorKey)
                .setContents(SMART_CONTRACT_BYTECODE)
                .execute(testEnv.client);

            var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

            response = new ContractCreateTransaction()
                .setAdminKey(testEnv.operatorKey)
                .setGas(200000)
                .setBytecodeFileId(fileId)
                .execute(testEnv.client);

            var contractId = Objects.requireNonNull(response.getReceipt(testEnv.client).contractId);

            Thread.sleep(2000);

            var gas = new MirrorNodeContractQuery()
                .setContractId(contractId)
                .setFunction("getOwner")
                .estimate(testEnv.client);

            var callQuery = new ContractCallQuery()
                .setContractId(contractId)
                .setGas(gas)
                .setFunction("getOwner")
                .setQueryPayment(new Hbar(1));

            var result = callQuery
                .execute(testEnv.client);

            var simulationResult = new MirrorNodeContractQuery()
                .setContractId(contractId)
                .setFunction("getOwner")
                .call(testEnv.client);

            assertThat(result.getAddress(0)).isEqualTo(simulationResult.substring(26));

            new ContractDeleteTransaction()
                .setTransferAccountId(testEnv.operatorId)
                .setContractId(contractId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new FileDeleteTransaction()
                .setFileId(fileId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        }
    }

    @Test
    @DisplayName("Can estimate and simulate transaction")
    void failsWhenContractIsNotDeployed() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {
            var contractId = new ContractId(1231456);

            assertThatExceptionOfType(ExecutionException.class).isThrownBy(() -> {
                new MirrorNodeContractQuery()
                    .setContractId(contractId)
                    .setFunction("getOwner")
                    .estimate(testEnv.client);
            }).withMessageContaining("Received non-200 response from Mirror Node");

            assertThatExceptionOfType(ExecutionException.class).isThrownBy(() -> {
                new MirrorNodeContractQuery()
                    .setContractId(contractId)
                    .setFunction("getOwner")
                    .call(testEnv.client);
            }).withMessageContaining("Received non-200 response from Mirror Node");
        }
    }
}
