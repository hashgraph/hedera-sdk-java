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
package com.hedera.hashgraph.sdk.examples;

import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.List;
import java.util.Objects;

public final class ContractNoncesExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(
        Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(
        Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");
    private static final String SMART_CONTRACT_BYTECODE = "6080604052348015600f57600080fd5b50604051601a90603b565b604051809103906000f0801580156035573d6000803e3d6000fd5b50506047565b605c8061009483390190565b603f806100556000396000f3fe6080604052600080fdfea2646970667358221220a20122cbad3457fedcc0600363d6e895f17048f5caa4afdab9e655123737567d64736f6c634300081200336080604052348015600f57600080fd5b50603f80601d6000396000f3fe6080604052600080fdfea264697066735822122053dfd8835e3dc6fedfb8b4806460b9b7163f8a7248bac510c6d6808d9da9d6d364736f6c63430008120033";

    private ContractNoncesExample() {
    }

    public static void main(String[] args) throws Exception {
        Client client = ClientHelper.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        PublicKey operatorPublicKey = OPERATOR_KEY.getPublicKey();

        TransactionResponse fileCreateTxResponse = new FileCreateTransaction()
            .setKeys(operatorPublicKey)
            .setContents(SMART_CONTRACT_BYTECODE)
            .setMaxTransactionFee(new Hbar(2)) // 2 HBAR
            .execute(client);

        TransactionReceipt fileCreateTxReceipt = fileCreateTxResponse.getReceipt(client);
        FileId newFileId = fileCreateTxReceipt.fileId;

        TransactionResponse contractCreateTxResponse = new ContractCreateTransaction()
            .setAdminKey(operatorPublicKey)
            .setGas(100_000)
            .setBytecodeFileId(newFileId)
            .setContractMemo("[e2e::ContractADeploysContractBInConstructor]")
            .execute(client);

        TransactionReceipt contractCreateTxReceipt = contractCreateTxResponse.getReceipt(client);

        ContractId contractId = contractCreateTxReceipt.contractId;

        List<ContractNonceInfo> contractNonces = contractCreateTxResponse.
            getRecord(client)
            .contractFunctionResult
            .contractNonces;

        System.out.println("contractNonces = " + contractNonces);

        // Clean up
        TransactionReceipt contractDeleteResult = new ContractDeleteTransaction()
            .setContractId(contractId)
            .setTransferAccountId(contractCreateTxReceipt.transactionId.accountId)
            .setMaxTransactionFee(new Hbar(1))
            .execute(client)
            .getReceipt(client);

        if (contractDeleteResult.status != Status.SUCCESS) {
            throw new Exception("error deleting contract: " + contractDeleteResult.status);
        }
        System.out.println("Contract successfully deleted");

        client.close();
    }
}
