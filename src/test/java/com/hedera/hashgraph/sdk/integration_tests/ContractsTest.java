package com.hedera.hashgraph.sdk.integration_tests;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.*;
import com.hedera.hashgraph.sdk.contract.ContractCreateTransaction;
import com.hedera.hashgraph.sdk.contract.ContractExecuteTransaction;
import com.hedera.hashgraph.sdk.contract.ContractFunctionParams;
import com.hedera.hashgraph.sdk.contract.ContractFunctionResult;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.file.FileCreateTransaction;
import com.hedera.hashgraph.sdk.file.FileId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class ContractsTest {

    private final TestEnv testEnv = new TestEnv();

    @Test
    void issue376() throws IOException, HederaStatusException {
        ClassLoader cl = getClass().getClassLoader();

        Gson gson = new Gson();

        JsonObject jsonObject;

        try (InputStream jsonStream = cl.getResourceAsStream("issue_376.json")) {
            if (jsonStream == null) {
                throw new RuntimeException("failed to get fail-constructor.json");
            }

            jsonObject = gson.fromJson(new InputStreamReader(jsonStream), JsonObject.class);
        }

        String byteCodeHex = jsonObject.getAsJsonPrimitive("object")
            .getAsString();

        Ed25519PrivateKey privateKey = Ed25519PrivateKey.generate();

        FileId bytecodeFile = new FileCreateTransaction()
            .setContents(byteCodeHex)
            // we don't care about editing this file later
            .addKey(privateKey.publicKey)
            .setMaxTransactionFee(new Hbar(20))
            .build(testEnv.client)
            .sign(privateKey)
            .execute(testEnv.client)
            .getReceipt(testEnv.client)
            .getFileId();

        TransactionReceipt createReceipt = new ContractCreateTransaction()
            .setMaxTransactionFee(new Hbar(20))
            .setBytecodeFileId(bytecodeFile)
            .setGas(1000)
            .setConstructorParams(new ContractFunctionParams().addString("error!"))
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        Assertions.assertEquals(Status.Success, createReceipt.status);

        TransactionId executeId = new ContractExecuteTransaction()
            .setContractId(createReceipt.getContractId())
            .setMaxTransactionFee(new Hbar(20))
            .setGas(10000)
            .setFunction("giveStrings", new ContractFunctionParams().addStringArray(new String[]{"string1", "string2"}))
            .execute(testEnv.client);

        ContractFunctionResult result = executeId.getRecord(testEnv.client).getContractExecuteResult();

        Assertions.assertEquals("string1", result.getString(0));
    }
}
