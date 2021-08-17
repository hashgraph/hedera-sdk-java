import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.ContractCreateTransaction;
import com.hedera.hashgraph.sdk.ContractFunctionParameters;
import com.hedera.hashgraph.sdk.FileCreateTransaction;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.SystemDeleteTransaction;
import com.hedera.hashgraph.sdk.SystemUndeleteTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SystemIntegrationTest {
    private static final String SMART_CONTRACT_BYTECODE = "608060405234801561001057600080fd5b506040516104d73803806104d78339818101604052602081101561003357600080fd5b810190808051604051939291908464010000000082111561005357600080fd5b90830190602082018581111561006857600080fd5b825164010000000081118282018810171561008257600080fd5b82525081516020918201929091019080838360005b838110156100af578181015183820152602001610097565b50505050905090810190601f1680156100dc5780820380516001836020036101000a031916815260200191505b506040525050600080546001600160a01b0319163317905550805161010890600190602084019061010f565b50506101aa565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061015057805160ff191683800117855561017d565b8280016001018555821561017d579182015b8281111561017d578251825591602001919060010190610162565b5061018992915061018d565b5090565b6101a791905b808211156101895760008155600101610193565b90565b61031e806101b96000396000f3fe608060405234801561001057600080fd5b50600436106100415760003560e01c8063368b87721461004657806341c0e1b5146100ee578063ce6d41de146100f6575b600080fd5b6100ec6004803603602081101561005c57600080fd5b81019060208101813564010000000081111561007757600080fd5b82018360208201111561008957600080fd5b803590602001918460018302840111640100000000831117156100ab57600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600092019190915250929550610173945050505050565b005b6100ec6101a2565b6100fe6101ba565b6040805160208082528351818301528351919283929083019185019080838360005b83811015610138578181015183820152602001610120565b50505050905090810190601f1680156101655780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b6000546001600160a01b0316331461018a5761019f565b805161019d906001906020840190610250565b505b50565b6000546001600160a01b03163314156101b85733ff5b565b60018054604080516020601f600260001961010087891615020190951694909404938401819004810282018101909252828152606093909290918301828280156102455780601f1061021a57610100808354040283529160200191610245565b820191906000526020600020905b81548152906001019060200180831161022857829003601f168201915b505050505090505b90565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061029157805160ff19168380011785556102be565b828001600101855582156102be579182015b828111156102be5782518255916020019190600101906102a3565b506102ca9291506102ce565b5090565b61024d91905b808211156102ca57600081556001016102d456fea264697066735822122084964d4c3f6bc912a9d20e14e449721012d625aa3c8a12de41ae5519752fc89064736f6c63430006000033";

    @Test
    @DisplayName("All system transactions are not supported")
    void allSystemTransactionsAreNotSupported() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv(1);

            var fileId = Objects.requireNonNull(
                new FileCreateTransaction()
                    .setContents(SMART_CONTRACT_BYTECODE)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client)
                    .fileId
            );

            var contractId = Objects.requireNonNull(
                new ContractCreateTransaction()
                    .setAdminKey(testEnv.operatorKey)
                    .setGas(2000)
                    .setConstructorParameters(new ContractFunctionParameters().addString("Hello from Hedera."))
                    .setBytecodeFileId(fileId)
                    .setContractMemo("[e2e::ContractCreateTransaction]")
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client)
                    .contractId
            );

            @Var var error = assertThrows(PrecheckStatusException.class, () -> {
                new SystemDeleteTransaction()
                    .setContractId(contractId)
                    .setExpirationTime(Instant.now())
                    .execute(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.NOT_SUPPORTED.toString()));

            error = assertThrows(PrecheckStatusException.class, () -> {
                new SystemDeleteTransaction()
                    .setFileId(fileId)
                    .setExpirationTime(Instant.now())
                    .execute(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.NOT_SUPPORTED.toString()));

            error = assertThrows(PrecheckStatusException.class, () -> {
                new SystemUndeleteTransaction()
                    .setContractId(contractId)
                    .execute(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.NOT_SUPPORTED.toString()));

            error = assertThrows(PrecheckStatusException.class, () -> {
                new SystemUndeleteTransaction()
                    .setFileId(fileId)
                    .execute(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.NOT_SUPPORTED.toString()));

            testEnv.close();
        });
    }
}
