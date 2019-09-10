package com.hedera.hashgraph.sdk.examples.advanced;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.CallParams;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.FunctionResult;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.contract.ContractCallQuery;
import com.hedera.hashgraph.sdk.contract.ContractCreateTransaction;
import com.hedera.hashgraph.sdk.contract.ContractExecuteTransaction;
import com.hedera.hashgraph.sdk.contract.ContractId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;
import com.hedera.hashgraph.sdk.file.FileCreateTransaction;
import com.hedera.hashgraph.sdk.file.FileId;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;

public final class CreateStatefulContract {
    private CreateStatefulContract() { }

    public static void main(String[] args) throws HederaException, IOException, InterruptedException {
        ClassLoader cl = CreateStatefulContract.class.getClassLoader();

        Gson gson = new Gson();

        JsonObject jsonObject;

        try (InputStream jsonStream = cl.getResourceAsStream("stateful.json")) {
            if (jsonStream == null) {
                throw new RuntimeException("failed to get stateful.json");
            }

            jsonObject = gson.fromJson(new InputStreamReader(jsonStream), JsonObject.class);
        }

        String byteCodeHex = jsonObject.getAsJsonPrimitive("object")
            .getAsString();
        byte[] byteCode = byteCodeHex.getBytes();

        Ed25519PrivateKey operatorKey = ExampleHelper.getOperatorKey();
        Client client = ExampleHelper.createHederaClient();

        // create the contract's bytecode file
        FileCreateTransaction fileTx = new FileCreateTransaction(client).setExpirationTime(
            Instant.now()
                .plus(Duration.ofSeconds(2592000)))
            // Use the same key as the operator to "own" this file
            .addKey(operatorKey.getPublicKey())
            .setContents(byteCode)
            .setTransactionFee(1_000_000_000);

        TransactionReceipt fileReceipt = fileTx.executeForReceipt();
        FileId newFileId = fileReceipt.getFileId();

        System.out.println("contract bytecode file: " + newFileId);

        ContractCreateTransaction contractTx = new ContractCreateTransaction(client).setBytecodeFile(newFileId)
            .setAutoRenewPeriod(Duration.ofHours(1))
            .setGas(100_000_000)
            .setTransactionFee(1_000_000_000)
            .setConstructorParams(
                CallParams.constructor()
                    .addString("hello from hedera!"));

        TransactionReceipt contractReceipt = contractTx.executeForReceipt();
        ContractId newContractId = contractReceipt.getContractId();

        System.out.println("new contract ID: " + newContractId);

        long gas = 1000;
        var query = new ContractCallQuery(client).setContractId(newContractId)
                .setGas(gas)
                .setFunctionParameters(CallParams.function("get_message"));

        long queryCost = query.requestCost();
        System.out.println("First contract local call estimated cost: " + queryCost);
        queryCost = (long) (queryCost * 1.1);
        
        FunctionResult contractCallResult = query
            .setPaymentDefault(queryCost)
            .execute();

        if (contractCallResult.getErrorMessage() != null) {
            System.out.println("error calling contract: " + contractCallResult.getErrorMessage());
            return;
        }

        String message = contractCallResult.getString();
        System.out.println("contract returned message: " + message);

        gas = 10_000;
        var record = new ContractExecuteTransaction(client).setContractId(newContractId)
            .setGas(gas)
            .setFunctionParameters(CallParams.function("set_message")
                .addString("hello from hedera again!"))
            .setTransactionFee(31_000_000)
            .executeForRecord();
        
        System.out.println("Contract function call transaction cost: " + record.getTransactionFee());
        System.out.println("Contract call result " + record.getReceipt().getStatus());

        // sleep a few seconds to allow consensus + smart contract exec
        System.out.println("Waiting 5s for consensus and contract execution");
        Thread.sleep(5000);
        
        // now query contract
        gas = 1000;
        query = new ContractCallQuery(client).setContractId(newContractId)
            .setGas(gas)
            .setFunctionParameters(CallParams.function("get_message"));
        
        queryCost = query.requestCost();
        System.out.println("Second contract local call estimated cost: " + queryCost);
        queryCost = (long) (queryCost * 1.1);

        FunctionResult contractUpdateResult = query
                .setPaymentDefault(queryCost)
                .execute();
        
        if (contractUpdateResult.getErrorMessage() != null) {
            System.out.println("error calling contract: " + contractUpdateResult.getErrorMessage());
            return;
        }

        String message2 = contractUpdateResult.getString();
        System.out.println("contract returned message: " + message2);
        
        System.out.println("");
        System.out.println("Example error conditions");
        System.out.println("");
        System.out.println("-> Setting gas too low - 200");
        try {
            gas = 200;
            query = new ContractCallQuery(client).setContractId(newContractId)
                    .setGas(gas)
                    .setFunctionParameters(CallParams.function("get_message"));
                
                contractUpdateResult = query
                    .setPaymentDefault(queryCost)
                    .execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Thread.sleep(1000);

        System.out.println("");
        System.out.println("-> Setting query fee too low - 20");
        try {
            gas = 1000;
            query = new ContractCallQuery(client).setContractId(newContractId)
                    .setGas(gas)
                    .setFunctionParameters(CallParams.function("get_message"));
                
                contractUpdateResult = query
                    .setPaymentDefault(20)
                    .execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Thread.sleep(1000);

        System.out.println("");
        System.out.println("-> Setting transaction fee too low - 1000");
        try {
            gas = 10_000;
            var receipt = new ContractExecuteTransaction(client).setContractId(newContractId)
                .setGas(gas)
                .setFunctionParameters(CallParams.function("set_message")
                    .addString("hello from hedera again!"))
                .setTransactionFee(1000)
                .executeForReceipt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
