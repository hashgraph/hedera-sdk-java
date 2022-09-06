import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.ContractCreateFlow;
import com.hedera.hashgraph.sdk.ContractExecuteTransaction;
import com.hedera.hashgraph.sdk.ContractFunctionParameters;
import com.hedera.hashgraph.sdk.ContractFunctionResult;
import com.hedera.hashgraph.sdk.ContractId;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.TransactionRecord;
import com.hedera.hashgraph.sdk.TransactionRecordQuery;
import com.hedera.hashgraph.sdk.TransactionResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;


/*
ContractHelper declutters PrecompileExample.java

When we instantiate a ContractHelper, we provide it with the JSON of a compiled solidity contract
which is assumed to have functions named "step0()" through "stepN()".

Each of these step functions is assumed to take no function parameters, and to return a Hedera ResponseCode
which ought to be SUCCESS -- in other words, an int32 with value 22.
See examples/src/main/resources/precompile-example/HederaResponseCodes.sol

If a step takes function parameters, or if its ContractFunctionResult should be validated with a different method,
the user can specify a supplier for a particular step with setParameterSupplier(stepIndex, parametersSupplier),
and can specify an alternative validation method with setResultValidator(stepIndex, validateFunction)

The contract is created on the Hedera network in the ContractHelper constructor, and when the user is ready to
execute the step functions in the contract, they should call executeSteps(firstStepToExecute, lastStepToExecute).
 */

public class ContractHelper {
    final ContractId contractId;
    final Map<Integer, Function<ContractFunctionResult, Boolean>> stepResultValidators = new HashMap<>();
    final Map<Integer, Supplier<ContractFunctionParameters>> stepParameterSuppliers = new HashMap<>();
    final Map<Integer, Hbar> stepPayableAmounts = new HashMap<>();
    final Map<Integer, List<PrivateKey>> stepSigners = new HashMap<>();
    final Map<Integer, AccountId> stepFeePayers = new HashMap<>();

    public static JsonObject getJsonResource(String filename) throws IOException {
        ClassLoader cl = ContractHelper.class.getClassLoader();

        Gson gson = new Gson();

        JsonObject jsonObject;

        try (InputStream jsonStream = cl.getResourceAsStream(filename)) {
            if (jsonStream == null) {
                throw new RuntimeException("failed to get " + filename);
            }

            jsonObject = gson.fromJson(new InputStreamReader(jsonStream, StandardCharsets.UTF_8), JsonObject.class);
        }
        return jsonObject;
    }

    public ContractHelper(
        JsonObject jsonObject,
        ContractFunctionParameters constructorParameters,
        Client client
    ) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        contractId = Objects.requireNonNull(new ContractCreateFlow()
            .setBytecode(jsonObject.getAsJsonPrimitive("object").getAsString())
            .setMaxChunks(30)
            .setGas(8_000_000)
            .setConstructorParameters(constructorParameters)
            .execute(client)
            .getReceipt(client)
            .contractId);
    }

    public ContractHelper setResultValidatorForStep(int stepIndex, Function<ContractFunctionResult, Boolean> validator) {
        stepResultValidators.put(stepIndex, validator);
        return this;
    }

    public ContractHelper setParameterSupplierForStep(int stepIndex, Supplier<ContractFunctionParameters> supplier) {
        stepParameterSuppliers.put(stepIndex, supplier);
        return this;
    }

    public ContractHelper setPayableAmountForStep(int stepIndex, Hbar amount) {
        stepPayableAmounts.put(stepIndex, amount);
        return this;
    }

    public ContractHelper addSignerForStep(int stepIndex, PrivateKey signer) {
        if (stepSigners.containsKey(stepIndex)) {
            stepSigners.get(stepIndex).add(signer);
        } else {
            List<PrivateKey> signerList = new ArrayList<>(1);
            signerList.add(signer);
            stepSigners.put(stepIndex, signerList);
        }
        return this;
    }

    public ContractHelper setFeePayerForStep(int stepIndex, AccountId feePayerAccount, PrivateKey feePayerKey) {
        stepFeePayers.put(stepIndex, feePayerAccount);
        return addSignerForStep(stepIndex, feePayerKey);
    }

    private Function<ContractFunctionResult, Boolean> getResultValidator(int stepIndex) {
        return stepResultValidators.getOrDefault(
            stepIndex,
            // if no custom validator is given, assume that the step returns a response code which ought to be SUCCESS
            contractFunctionResult -> {
                Status responseStatus = Status.fromResponseCode(contractFunctionResult.getInt32(0));
                boolean isValid = responseStatus == Status.SUCCESS;
                if (!isValid) {
                    System.out.println("Encountered invalid response code " + responseStatus.toResponseCode());
                }
                return isValid;
            }
        );
    }

    private Supplier<ContractFunctionParameters> getParameterSupplier(int stepIndex) {
        return stepParameterSuppliers.getOrDefault(stepIndex, () -> null);
    }

    private Hbar getPayableAmount(int stepIndex) {
        return stepPayableAmounts.get(stepIndex);
    }

    private List<PrivateKey> getSigners(int stepIndex) {
        return stepSigners.getOrDefault(stepIndex, Collections.emptyList());
    }

    public ContractHelper executeSteps(
        int firstStepToExecute,
        int lastStepToExecute,
        Client client
    ) throws Exception {
        for (int stepIndex = firstStepToExecute; stepIndex <= lastStepToExecute; stepIndex++) {
            System.out.println("Attempting to execute step " + stepIndex);
            ContractExecuteTransaction tx = new ContractExecuteTransaction()
                .setContractId(contractId)
                .setGas(10_000_000);

            Hbar payableAmount = getPayableAmount(stepIndex);
            if (payableAmount != null) {
                tx.setPayableAmount(payableAmount);
            }

            String functionName = "step" + stepIndex;
            ContractFunctionParameters parameters = getParameterSupplier(stepIndex).get();
            if (parameters != null) {
                tx.setFunction(functionName, parameters);
            } else {
                tx.setFunction(functionName);
            }

            AccountId feePayerAccountId = stepFeePayers.get(stepIndex);
            if (feePayerAccountId != null) {
                tx.setTransactionId(TransactionId.generate(feePayerAccountId));
            }

            tx.freezeWith(client);
            for (PrivateKey signer : getSigners(stepIndex)) {
                tx.sign(signer);
            }

            TransactionRecord record = tx
                .execute(client)
                .getRecord(client);

            ContractFunctionResult functionResult = Objects.requireNonNull(record.contractFunctionResult);

            if (getResultValidator(stepIndex).apply(functionResult)) {
                System.out.println("step " + stepIndex + " completed, and returned valid result. (TransactionId \"" + record.transactionId + "\")");
            } else {
                System.out.println("Transaction record: " + record);
                throw new Exception("step " + stepIndex + " returned invalid result");
            }
        }
        return this;
    }
}
