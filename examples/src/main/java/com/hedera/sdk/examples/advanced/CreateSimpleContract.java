package com.hedera.sdk.examples.advanced;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hedera.sdk.CallParams;
import com.hedera.sdk.HederaException;
import com.hedera.sdk.contract.ContractCallQuery;
import com.hedera.sdk.contract.ContractCreateTransaction;
import com.hedera.sdk.examples.ExampleHelper;
import com.hedera.sdk.file.FileCreateTransaction;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

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
        var newFileId = Objects.requireNonNull(fileReceipt.getFileId());

        System.out.println("contract bytecode file: " + newFileId);

        // create the contract itself
        var contractTx = new ContractCreateTransaction(client).setAutoRenewPeriod(Duration.ofHours(1))
            .setGas(217000)
            .setBytecodeFile(newFileId);

        var contractReceipt = contractTx.executeForReceipt();

        System.out.println(contractReceipt.toProto());

        var newContractId = Objects.requireNonNull(contractReceipt.getContractId());

        System.out.println("new contract ID: " + newContractId);

        var contractCallResult = new ContractCallQuery(client).setGas(30000).setContract(newContractId)
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
