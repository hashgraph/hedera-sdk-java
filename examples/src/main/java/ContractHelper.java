import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.ContractCreateFlow;
import com.hedera.hashgraph.sdk.ContractExecuteTransaction;
import com.hedera.hashgraph.sdk.ContractFunctionParameters;
import com.hedera.hashgraph.sdk.ContractFunctionResult;
import com.hedera.hashgraph.sdk.ContractId;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.ReceiptStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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
execute the step functions in the contract, they should call executeSteps(stepsCount).
 */

public class ContractHelper {
    final ContractId contractId;
    final Map<Integer, Function<ContractFunctionResult, Boolean>> stepResultValidators = new HashMap<>();
    final Map<Integer, Supplier<ContractFunctionParameters>> stepParameterSuppliers = new HashMap<>();

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
            .setGas(8_000_000)
            .setConstructorParameters(constructorParameters)
            .execute(client)
            .getReceipt(client)
            .contractId);
    }

    public ContractHelper setResultValidator(int stepIndex, Function<ContractFunctionResult, Boolean> validator) {
        stepResultValidators.put(stepIndex, validator);
        return this;
    }

    public ContractHelper setParameterSupplier(int stepIndex, Supplier<ContractFunctionParameters> supplier) {
        stepParameterSuppliers.put(stepIndex, supplier);
        return this;
    }

    private Function<ContractFunctionResult, Boolean> getResultValidator(int stepIndex) {
        return stepResultValidators.getOrDefault(
            stepIndex,
            // if no custom validator is given, assume that the step returns a response code which ought to be SUCCESS
            contractFunctionResult -> contractFunctionResult.getInt32(0) == 22 /* SUCCESS */
        );
    }

    private Supplier<ContractFunctionParameters> getParameterSupplier(int stepIndex) {
        return stepParameterSuppliers.getOrDefault(stepIndex, () -> null);
    }

    public void executeSteps(
        int stepsCount,
        Client client
    ) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        for (int stepIndex = 0; stepIndex < stepsCount; stepIndex++) {
            ContractExecuteTransaction tx = new ContractExecuteTransaction()
                .setContractId(contractId)
                .setGas(8_000_000);

            String functionName = "step" + stepIndex;
            ContractFunctionParameters parameters = getParameterSupplier(stepIndex).get();
            if (parameters != null) {
                tx.setFunction(functionName, parameters);
            } else {
                tx.setFunction(functionName);
            }

            ContractFunctionResult functionResult = Objects.requireNonNull(tx
                .execute(client)
                .getRecord(client)
                .contractFunctionResult
            );

            if (functionResult.errorMessage == null && getResultValidator(stepIndex).apply(functionResult)) {
                System.out.println("step " + stepIndex + " completed, and returned valid result.");
            } else {
                System.out.println("ERROR: step " + stepIndex + " returned invalid result: " + functionResult);
                return;
            }
        }

        System.out.println("All steps completed with valid results.");
    }
}
