package com.hedera.hashgraph.sdk.examples.advanced;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.CallParams;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.contract.ContractCallQuery;
import com.hedera.hashgraph.sdk.contract.ContractCreateTransaction;
import com.hedera.hashgraph.sdk.contract.ContractExecuteTransaction;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;
import com.hedera.hashgraph.sdk.file.FileCreateTransaction;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;

public final class CreateStatefulContract {
    private CreateStatefulContract() { }

    public static void main(String[] args) throws HederaException, IOException, InterruptedException {
        var cl = CreateStatefulContract.class.getClassLoader();

        var gson = new Gson();

        JsonObject jsonObject;

        try (var jsonStream = cl.getResourceAsStream("stateful.json")) {
            if (jsonStream == null) {
                throw new RuntimeException("failed to get stateful.json");
            }

            jsonObject = gson.fromJson(new InputStreamReader(jsonStream), JsonObject.class);
        }

        var byteCodeHex = jsonObject.getAsJsonPrimitive("object")
            .getAsString();
        var byteCode = byteCodeHex.getBytes();

        var operatorKey = ExampleHelper.getOperatorKey();
        var client = ExampleHelper.createHederaClient();

        // create the contract's bytecode file
        var fileTx = new FileCreateTransaction(client).setExpirationTime(
            Instant.now()
                .plus(Duration.ofSeconds(2592000)))
            // Use the same key as the operator to "own" this file
            .addKey(operatorKey.getPublicKey())
            .setContents(byteCode);

        var fileReceipt = fileTx.executeForReceipt();
        var newFileId = fileReceipt.getFileId();

        System.out.println("contract bytecode file: " + newFileId);

        var contractTx = new ContractCreateTransaction(client).setBytecodeFile(newFileId)
            .setAutoRenewPeriod(Duration.ofHours(1))
            .setGas(100_000_000)
            .setConstructorParams(
                CallParams.constructor()
                    .addString("hello from hedera!"));

        var contractReceipt = contractTx.executeForReceipt();
        var newContractId = contractReceipt.getContractId();

        System.out.println("new contract ID: " + newContractId);

        var contractCallResult = new ContractCallQuery(client).setContractId(newContractId)
            .setGas(100_000_000)
            .setFunctionParameters(CallParams.function("get_message"))
            .execute();

        if (contractCallResult.getErrorMessage() != null) {
            System.out.println("error calling contract: " + contractCallResult.getErrorMessage());
            return;
        }

        var message = contractCallResult.getString();
        System.out.println("contract returned message: " + message);

        new ContractExecuteTransaction(client).setContractId(newContractId)
            .setGas(100_000_000)
            .setFunctionParameters(CallParams.function("set_message")
                .addString("hello from hedera again!"))
            .execute();

        // sleep a few seconds to allow consensus + smart contract exec
        System.out.println("Waiting 5s for consensus and contract execution");
        Thread.sleep(5000);
        // now query contract
        var contractUpdateResult = new ContractCallQuery(client).setContractId(newContractId)
            .setGas(100_000_000)
            .setFunctionParameters(CallParams.function("get_message"))
            .execute();

        if (contractUpdateResult.getErrorMessage() != null) {
            System.out.println("error calling contract: " + contractUpdateResult.getErrorMessage());
            return;
        }

        var message2 = contractUpdateResult.getString();
        System.out.println("contract returned message: " + message2);
    }
}
