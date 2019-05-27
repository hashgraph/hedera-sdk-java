package com.hedera.hashgraph.sdk.examples.advanced;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.CallParams;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.contract.ContractCallQuery;
import com.hedera.hashgraph.sdk.contract.ContractCreateTransaction;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;
import com.hedera.hashgraph.sdk.file.FileCreateTransaction;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;

public final class CreateSimpleContract {
    public static void main(String[] args) throws HederaException, IOException {
        var cl = CreateSimpleContract.class.getClassLoader();

        var gson = new Gson();

        JsonObject jsonObject;

        try (var jsonStream = cl.getResourceAsStream("hello_world.json")) {
            if (jsonStream == null) {
                throw new RuntimeException("failed to get hello_world.json");
            }

            jsonObject = gson.fromJson(new InputStreamReader(jsonStream), JsonObject.class);
        }

        var byteCodeHex = jsonObject.getAsJsonPrimitive("object")
            .getAsString();

        var operatorKey = ExampleHelper.getOperatorKey();
        var client = ExampleHelper.createHederaClient();

        // create the contract's bytecode file
        var fileTx = new FileCreateTransaction(client).setExpirationTime(
            Instant.now()
                .plus(Duration.ofSeconds(3600))
        )
            // Use the same key as the operator to "own" this file
            .addKey(operatorKey.getPublicKey())
            .setContents(byteCodeHex.getBytes());

        var fileReceipt = fileTx.executeForReceipt();
        var newFileId = fileReceipt.getFileId();

        System.out.println("contract bytecode file: " + newFileId);

        // create the contract itself
        var contractTx = new ContractCreateTransaction(client).setAutoRenewPeriod(Duration.ofHours(1))
            .setGas(217000)
            .setBytecodeFile(newFileId);

        var contractReceipt = contractTx.executeForReceipt();

        System.out.println(contractReceipt.toProto());

        var newContractId = contractReceipt.getContractId();

        System.out.println("new contract ID: " + newContractId);

        var contractCallResult = new ContractCallQuery(client).setGas(30000)
            .setContractId(newContractId)
            .setFunctionParameters(CallParams.function("greet"))
            .execute();

        if (contractCallResult.getErrorMessage() != null) {
            System.out.println("error calling contract: " + contractCallResult.getErrorMessage());
            return;
        }

        var message = contractCallResult.getString();
        System.out.println("contract message: " + message);
    }
}
