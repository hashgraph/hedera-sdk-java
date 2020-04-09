import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class ContractIntegrationTest {
    @Test
    void test() {
        assertDoesNotThrow(() -> {
            var operatorKey = PrivateKey.fromString("302e020100300506032b65700422042091dad4f120ca225ce66deb1d6fb7ecad0e53b5e879aa45b0c5e0db7923f26d08");
            var operatorId = new AccountId(147722);

            // TODO: Read from file
            var smartContractBytecode = "";

            var client = Client.forTestnet()
                .setOperator(operatorId, operatorKey);

            @Var var transactionId = new FileCreateTransaction()
                .addKey(operatorKey.getPublicKey())
                .setContents(smartContractBytecode)
                .setMaxTransactionFee(new Hbar(5))
                .execute(client);

            @Var var receipt = new TransactionReceiptQuery()
                .setTransactionId(transactionId)
                .execute(client);

            assertNotNull(receipt.fileId);
            assertEquals(receipt.status, Status.Success);
            assertTrue(Objects.requireNonNull(receipt.fileId).num > 0);

            var file = receipt.fileId;

            transactionId = new ContractCreateTransaction()
                .setAdminKey(operatorKey.getPublicKey())
                .setGas(2000)
                .setConstructorParameters(new ContractFunctionParameters().addString("Hello from Hedera."))
                .setByteCodeFileId(file)
                .setContractMemo("[e2e::ContractCreateTransaction]")
                .setMaxTransactionFee(new Hbar(20))
                .execute(client);

            receipt = new TransactionReceiptQuery()
                .setTransactionId(transactionId)
                .execute(client);

            assertNotNull(receipt.contractId);
            assertEquals(receipt.status, Status.Success);
            assertTrue(Objects.requireNonNull(receipt.contractId).num > 0);

            var contract = Objects.requireNonNull(receipt.contractId);

            @Var var info = new ContractInfoQuery()
                .setContractId(contract)
                .execute(client);

            assertEquals(info.contractId, contract);
            assertNotNull(info.accountId);
            assertEquals(Objects.requireNonNull(info.accountId).toString(), contract.toString());
            assertNotNull(info.adminKey);
            assertEquals(Objects.requireNonNull(info.adminKey).toString(), operatorKey.getPublicKey().toString());
            assertEquals(info.storage, 926);
            assertEquals(info.contractMemo, "[e2e::ContractCreateTransaction]");

            var bytecode = new ContractByteCodeQuery()
                .setContractId(contract)
                .setMaxQueryPayment(new Hbar(2))
                .execute(client);

            assertEquals(bytecode.size(), 798);

            @Var var result = new ContractCallQuery()
                .setContractId(contract)
                .setGas(2000)
                .setFunction("getMessage")
                .execute(client);

            assertEquals(result.getString(0), "Hello from Hedera");

            transactionId = new ContractExecuteTransaction()
                .setContractId(contract)
                .setGas(2000)
                .setFunction("setMessage", new ContractFunctionParameters().addString("new message"))
                .setMaxTransactionFee(new Hbar(5))
                .execute(client);

            new TransactionReceiptQuery()
                .setTransactionId(transactionId)
                .execute(client);

            result = new ContractCallQuery()
                .setContractId(contract)
                .setGas(2000)
                .setFunction("getMessage")
                .execute(client);

            assertEquals(result.getString(0), "new message");

            new ContractRecordsQuery()
                .setContractId(contract)
                .setMaxQueryPayment(new Hbar(5))
                .execute(client);

            transactionId = new ContractUpdateTransaction()
                .setContractId(contract)
                .setContractMemo("[e2e::ContractUpdateTransaction]")
                .setMaxTransactionFee(new Hbar(5))
                .execute(client);

            new TransactionReceiptQuery()
                .setTransactionId(transactionId)
                .execute(client);

            info = new ContractInfoQuery()
                .setContractId(contract)
                .execute(client);

            assertEquals(info.contractId, contract);
            assertNotNull(info.accountId);
            assertEquals(Objects.requireNonNull(info.accountId).toString(), contract.toString());            assertNotNull(info.adminKey);
            assertEquals(Objects.requireNonNull(info.adminKey).toString(), operatorKey.getPublicKey().toString());
            assertEquals(info.storage, 926);
            assertEquals(info.contractMemo, "[e2e::ContractUpdateTransaction]");

            transactionId = new ContractDeleteTransaction()
                .setContractId(contract)
                .execute(client);

            new TransactionReceiptQuery()
                .setTransactionId(transactionId)
                .execute(client);

            assertThrows(Exception.class, () -> {
                new ContractInfoQuery()
                    .setContractId(contract)
                    .setMaxQueryPayment(new Hbar(2))
                    .execute(client);
            });
        });
    }
}
