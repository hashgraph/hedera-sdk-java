import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.ContractCallQuery;
import com.hedera.hashgraph.sdk.ContractCreateTransaction;
import com.hedera.hashgraph.sdk.ContractDeleteTransaction;
import com.hedera.hashgraph.sdk.ContractFunctionParameters;
import com.hedera.hashgraph.sdk.ContractId;
import com.hedera.hashgraph.sdk.FileAppendTransaction;
import com.hedera.hashgraph.sdk.FileCreateTransaction;
import com.hedera.hashgraph.sdk.FileDeleteTransaction;
import com.hedera.hashgraph.sdk.FileId;
import com.hedera.hashgraph.sdk.Hbar;
import java.math.BigInteger;
import java.util.Objects;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Check Go & JS examples for workaround re not implemented funcs
// Test int and uint arrays?
// Revisit test func names
public class ContractFunctionParametersIntegrationTest {

    private static final String SMART_CONTRACT_BYTECODE = "608060405234801561001057600080fd5b50611f83806100206000396000f3fe608060405234801561001057600080fd5b50600436106104805760003560e01c806381dbe13e11610257578063bb6b524311610146578063dbb04ed9116100c3578063e713cda811610087578063e713cda814610df6578063f4e490f514610e19578063f6e877f414610e3a578063f8293f6e14610e60578063ffb8050114610e8257600080fd5b8063dbb04ed914610d2d578063de9fb48414610d56578063e05e91e014610d83578063e066de5014610daa578063e0f53e2414610dd057600080fd5b8063cbd2e6a51161010a578063cbd2e6a514610c9a578063cdb9e4e814610cbf578063d1b10ad71461066d578063d79d4d4014610ce5578063dade0c0b14610d0b57600080fd5b8063bb6b524314610bdc578063bd90536a14610c02578063c503772d14610c2a578063c6c18a1c14610c4a578063c7d8b87e14610c7457600080fd5b8063a1bda122116101d4578063b4e3e7b111610198578063b4e3e7b114610b28578063b834bfe914610b4e578063b8da8d1614610b6f578063b989c7ee14610b95578063ba945bdb14610bb657600080fd5b8063a1bda12214610a9f578063a401d60d14610ac0578063a75761f114610ae6578063aa80ca2e14610733578063b2db404a14610b0757600080fd5b8063923f5edf1161021b578063923f5edf146109f557806394cd7c8014610a1657806398508ba314610a375780639b1794ae14610a58578063a08b9f6714610a7e57600080fd5b806381dbe13e14610978578063827147ce146105cf578063881c8fb71461099357806388b7e6f5146109b9578063909c5b24146109da57600080fd5b806338fa665811610373578063628bc3ef116102f057806372a06b4d116102b457806372a06b4d146108ef578063796a27ea146109105780637d0dc262146109365780637ec32d84146109575780637f8082f71461073357600080fd5b8063628bc3ef1461083f57806364e008c11461086057806368ef4466146108815780636a54715c146108a257806370a5cb81146108c357600080fd5b806344e7b0371161033757806344e7b0371461068957806348d848d0146107d95780634bbc9a67146107f7578063545e21131461081257806359adb2df1461066d57600080fd5b806338fa6658146107335780633b45e6e01461074e5780633e1a27711461076f5780633f396e6714610790578063407b899b146107b857600080fd5b8063129ed5da116104015780632421101f116103c55780632421101f146106895780632ef16e8e146106af5780632f47a40d146106d05780632f6c1bb4146106f157806333520ec31461071257600080fd5b8063129ed5da146105ea57806312cd95a114610610578063189cea8e146106315780631d1145621461065257806322937ea91461066d57600080fd5b80630a958dc8116104485780630a958dc81461054b57806310d545531461056c578063118b84151461058d57806311ec6c90146105ae578063126bc815146105cf57600080fd5b8063017fa10b14610485578063021d88ab146104b357806303745430146104de57806306ac6fe1146104ff57806308123e0914610525575b600080fd5b6104966104933660046117ac565b90565b6040516001600160801b0390911681526020015b60405180910390f35b6104c1610493366004611b97565b6040516bffffffffffffffffffffffff90911681526020016104aa565b6104ec610493366004611333565b604051600c9190910b81526020016104aa565b61050d610493366004611785565b6040516001600160781b0390911681526020016104aa565b610533610493366004611aa9565b60405166ffffffffffffff90911681526020016104aa565b610559610493366004611609565b60405160049190910b81526020016104aa565b61057a6104933660046113d8565b60405160119190910b81526020016104aa565b61059b6104933660046115c7565b604051601e9190910b81526020016104aa565b6105bc61049336600461143b565b60405160139190910b81526020016104aa565b6105dd6104933660046112f8565b6040516104aa9190611d82565b6105f8610493366004611821565b6040516001600160981b0390911681526020016104aa565b61061e6104933660046113f9565b60405160129190910b81526020016104aa565b61063f61049336600461149e565b60405160169190910b81526020016104aa565b610660610493366004610fbd565b6040516104aa9190611c0e565b61067b6104933660046112e0565b6040519081526020016104aa565b610697610493366004610f9a565b6040516001600160a01b0390911681526020016104aa565b6106bd6104933660046115a6565b604051601d9190910b81526020016104aa565b6106de6104933660046116ef565b604051600a9190910b81526020016104aa565b6106ff610493366004611501565b60405160199190910b81526020016104aa565b610720610493366004611522565b604051601a9190910b81526020016104aa565b6107416104933660046110ec565b6040516104aa9190611c95565b61075c6104933660046113b7565b60405160109190910b81526020016104aa565b61077d610493366004611564565b604051601c9190910b81526020016104aa565b61079e610493366004611af8565b60405168ffffffffffffffffff90911681526020016104aa565b6107c661049336600461166c565b60405160079190910b81526020016104aa565b6107e76104933660046112c6565b60405190151581526020016104aa565b61080561049336600461105e565b6040516104aa9190611c5b565b6108256108203660046116ae565b610ea6565b60408051600093840b81529190920b6020820152016104aa565b61084d6104933660046116ce565b60405160099190910b81526020016104aa565b61086e6104933660046114bf565b60405160179190910b81526020016104aa565b61088f61049336600461145c565b60405160149190910b81526020016104aa565b6108b061049336600461164b565b60405160069190910b81526020016104aa565b6108d1610493366004611731565b6040516cffffffffffffffffffffffffff90911681526020016104aa565b6108fd6104933660046116ae565b60405160009190910b81526020016104aa565b61091e610493366004611954565b6040516001600160d81b0390911681526020016104aa565b610944610493366004611543565b604051601b9190910b81526020016104aa565b610965610493366004611585565b60405160029190910b81526020016104aa565b610986610493366004611224565b6040516104aa9190611d2e565b6109a1610493366004611891565b6040516001600160b01b0390911681526020016104aa565b6109c7610493366004611396565b604051600f9190910b81526020016104aa565b6109e8610493366004611173565b6040516104aa9190611ccd565b610a0361049336600461147d565b60405160159190910b81526020016104aa565b610a246104933660046114e0565b60405160189190910b81526020016104aa565b610a45610493366004611354565b604051600d9190910b81526020016104aa565b610a666104933660046118b8565b6040516001600160b81b0390911681526020016104aa565b610a8c610493366004611710565b604051600b9190910b81526020016104aa565b610aad61049336600461141a565b60405160019190910b81526020016104aa565b610ace6104933660046119ec565b6040516001600160f01b0390911681526020016104aa565b610af4610493366004611848565b60405161ffff90911681526020016104aa565b610b1561049336600461162a565b60405160059190910b81526020016104aa565b610b3661049336600461175e565b6040516001600160701b0390911681526020016104aa565b610b5c610493366004611375565b604051600e9190910b81526020016104aa565b610b7d61049336600461186a565b6040516001600160a81b0390911681526020016104aa565b610ba36104933660046115e8565b60405160039190910b81526020016104aa565b610bc46104933660046117d3565b6040516001600160881b0390911681526020016104aa565b610bea610493366004611906565b6040516001600160c81b0390911681526020016104aa565b610c15610c103660046112e0565b610ebe565b604080519283526020830191909152016104aa565b610c38610493366004611b21565b60405160ff90911681526020016104aa565b610c58610493366004611b6c565b6040516affffffffffffffffffffff90911681526020016104aa565b610c82610493366004611a13565b6040516001600160f81b0390911681526020016104aa565b610ca8610493366004611a83565b60405165ffffffffffff90911681526020016104aa565b610ccd61049336600461197b565b6040516001600160e01b0390911681526020016104aa565b610cf361049336600461192d565b6040516001600160d01b0390911681526020016104aa565b610d1e610d19366004611a3a565b610ecd565b6040516104aa93929190611d95565b610d3b610493366004611b42565b60405169ffffffffffffffffffff90911681526020016104aa565b610d69610d64366004611609565b610f0b565b60408051600493840b81529190920b6020820152016104aa565b610d91610493366004611ad0565b60405167ffffffffffffffff90911681526020016104aa565b610db86104933660046119a2565b6040516001600160e81b0390911681526020016104aa565b610dde6104933660046118df565b6040516001600160c01b0390911681526020016104aa565b610e04610493366004611a3a565b60405163ffffffff90911681526020016104aa565b610e2761049336600461168d565b60405160089190910b81526020016104aa565b610e486104933660046117fa565b6040516001600160901b0390911681526020016104aa565b610e6e6104933660046119c9565b60405162ffffff90911681526020016104aa565b610e90610493366004611a5e565b60405164ffffffffff90911681526020016104aa565b60008082610eb5816014611ead565b91509150915091565b60008082610eb5816001611e22565b600080606083610ede600182611ee4565b6040805180820190915260028152614f4b60f01b602082015291945063ffffffff16925090509193909250565b60008082610eb5816001611e63565b80358015158114610f2a57600080fd5b919050565b600082601f830112610f3f578081fd5b813567ffffffffffffffff811115610f5957610f59611f1f565b610f6c601f8201601f1916602001611dcd565b818152846020838601011115610f80578283fd5b816020850160208301379081016020019190915292915050565b600060208284031215610fab578081fd5b8135610fb681611f35565b9392505050565b60006020808385031215610fcf578182fd5b823567ffffffffffffffff811115610fe5578283fd5b8301601f81018513610ff5578283fd5b803561100861100382611dfe565b611dcd565b80828252848201915084840188868560051b8701011115611027578687fd5b8694505b8385101561105257803561103e81611f35565b83526001949094019391850191850161102b565b50979650505050505050565b60006020808385031215611070578182fd5b823567ffffffffffffffff811115611086578283fd5b8301601f81018513611096578283fd5b80356110a461100382611dfe565b80828252848201915084840188868560051b87010111156110c3578687fd5b8694505b83851015611052576110d881610f1a565b8352600194909401939185019185016110c7565b600060208083850312156110fe578182fd5b823567ffffffffffffffff811115611114578283fd5b8301601f81018513611124578283fd5b803561113261100382611dfe565b80828252848201915084840188868560051b8701011115611151578687fd5b8694505b83851015611052578035835260019490940193918501918501611155565b60006020808385031215611185578182fd5b823567ffffffffffffffff8082111561119c578384fd5b818501915085601f8301126111af578384fd5b81356111bd61100382611dfe565b80828252858201915085850189878560051b88010111156111dc578788fd5b875b84811015611215578135868111156111f457898afd5b6112028c8a838b0101610f2f565b85525092870192908701906001016111de565b50909998505050505050505050565b60006020808385031215611236578182fd5b823567ffffffffffffffff8082111561124d578384fd5b818501915085601f830112611260578384fd5b813561126e61100382611dfe565b80828252858201915085850189878560051b880101111561128d578788fd5b875b84811015611215578135868111156112a557898afd5b6112b38c8a838b0101610f2f565b855250928701929087019060010161128f565b6000602082840312156112d7578081fd5b610fb682610f1a565b6000602082840312156112f1578081fd5b5035919050565b600060208284031215611309578081fd5b813567ffffffffffffffff81111561131f578182fd5b61132b84828501610f2f565b949350505050565b600060208284031215611344578081fd5b813580600c0b8114610fb6578182fd5b600060208284031215611365578081fd5b813580600d0b8114610fb6578182fd5b600060208284031215611386578081fd5b813580600e0b8114610fb6578182fd5b6000602082840312156113a7578081fd5b813580600f0b8114610fb6578182fd5b6000602082840312156113c8578081fd5b81358060100b8114610fb6578182fd5b6000602082840312156113e9578081fd5b81358060110b8114610fb6578182fd5b60006020828403121561140a578081fd5b81358060120b8114610fb6578182fd5b60006020828403121561142b578081fd5b81358060010b8114610fb6578182fd5b60006020828403121561144c578081fd5b81358060130b8114610fb6578182fd5b60006020828403121561146d578081fd5b81358060140b8114610fb6578182fd5b60006020828403121561148e578081fd5b81358060150b8114610fb6578182fd5b6000602082840312156114af578081fd5b81358060160b8114610fb6578182fd5b6000602082840312156114d0578081fd5b81358060170b8114610fb6578182fd5b6000602082840312156114f1578081fd5b81358060180b8114610fb6578182fd5b600060208284031215611512578081fd5b81358060190b8114610fb6578182fd5b600060208284031215611533578081fd5b813580601a0b8114610fb6578182fd5b600060208284031215611554578081fd5b813580601b0b8114610fb6578182fd5b600060208284031215611575578081fd5b813580601c0b8114610fb6578182fd5b600060208284031215611596578081fd5b81358060020b8114610fb6578182fd5b6000602082840312156115b7578081fd5b813580601d0b8114610fb6578182fd5b6000602082840312156115d8578081fd5b813580601e0b8114610fb6578182fd5b6000602082840312156115f9578081fd5b81358060030b8114610fb6578182fd5b60006020828403121561161a578081fd5b81358060040b8114610fb6578182fd5b60006020828403121561163b578081fd5b81358060050b8114610fb6578182fd5b60006020828403121561165c578081fd5b81358060060b8114610fb6578182fd5b60006020828403121561167d578081fd5b81358060070b8114610fb6578182fd5b60006020828403121561169e578081fd5b81358060080b8114610fb6578182fd5b6000602082840312156116bf578081fd5b813580820b8114610fb6578182fd5b6000602082840312156116df578081fd5b81358060090b8114610fb6578182fd5b600060208284031215611700578081fd5b813580600a0b8114610fb6578182fd5b600060208284031215611721578081fd5b813580600b0b8114610fb6578182fd5b600060208284031215611742578081fd5b81356cffffffffffffffffffffffffff81168114610fb6578182fd5b60006020828403121561176f578081fd5b81356001600160701b0381168114610fb6578182fd5b600060208284031215611796578081fd5b81356001600160781b0381168114610fb6578182fd5b6000602082840312156117bd578081fd5b81356001600160801b0381168114610fb6578182fd5b6000602082840312156117e4578081fd5b81356001600160881b0381168114610fb6578182fd5b60006020828403121561180b578081fd5b81356001600160901b0381168114610fb6578182fd5b600060208284031215611832578081fd5b81356001600160981b0381168114610fb6578182fd5b600060208284031215611859578081fd5b813561ffff81168114610fb6578182fd5b60006020828403121561187b578081fd5b81356001600160a81b0381168114610fb6578182fd5b6000602082840312156118a2578081fd5b81356001600160b01b0381168114610fb6578182fd5b6000602082840312156118c9578081fd5b81356001600160b81b0381168114610fb6578182fd5b6000602082840312156118f0578081fd5b81356001600160c01b0381168114610fb6578182fd5b600060208284031215611917578081fd5b81356001600160c81b0381168114610fb6578182fd5b60006020828403121561193e578081fd5b81356001600160d01b0381168114610fb6578182fd5b600060208284031215611965578081fd5b81356001600160d81b0381168114610fb6578182fd5b60006020828403121561198c578081fd5b81356001600160e01b0381168114610fb6578182fd5b6000602082840312156119b3578081fd5b81356001600160e81b0381168114610fb6578182fd5b6000602082840312156119da578081fd5b813562ffffff81168114610fb6578182fd5b6000602082840312156119fd578081fd5b81356001600160f01b0381168114610fb6578182fd5b600060208284031215611a24578081fd5b81356001600160f81b0381168114610fb6578182fd5b600060208284031215611a4b578081fd5b813563ffffffff81168114610fb6578182fd5b600060208284031215611a6f578081fd5b813564ffffffffff81168114610fb6578182fd5b600060208284031215611a94578081fd5b813565ffffffffffff81168114610fb6578182fd5b600060208284031215611aba578081fd5b813566ffffffffffffff81168114610fb6578182fd5b600060208284031215611ae1578081fd5b813567ffffffffffffffff81168114610fb6578182fd5b600060208284031215611b09578081fd5b813568ffffffffffffffffff81168114610fb6578182fd5b600060208284031215611b32578081fd5b813560ff81168114610fb6578182fd5b600060208284031215611b53578081fd5b813569ffffffffffffffffffff81168114610fb6578182fd5b600060208284031215611b7d578081fd5b81356affffffffffffffffffffff81168114610fb6578182fd5b600060208284031215611ba8578081fd5b81356bffffffffffffffffffffffff81168114610fb6578182fd5b60008151808452815b81811015611be857602081850181015186830182015201611bcc565b81811115611bf95782602083870101525b50601f01601f19169290920160200192915050565b6020808252825182820181905260009190848201906040850190845b81811015611c4f5783516001600160a01b031683529284019291840191600101611c2a565b50909695505050505050565b6020808252825182820181905260009190848201906040850190845b81811015611c4f578351151583529284019291840191600101611c77565b6020808252825182820181905260009190848201906040850190845b81811015611c4f57835183529284019291840191600101611cb1565b6000602080830181845280855180835260408601915060408160051b8701019250838701855b82811015611d2157603f19888603018452611d0f858351611bc3565b94509285019290850190600101611cf3565b5092979650505050505050565b6000602080830181845280855180835260408601915060408160051b8701019250838701855b82811015611d2157603f19888603018452611d70858351611bc3565b94509285019290850190600101611d54565b602081526000610fb66020830184611bc3565b63ffffffff8416815267ffffffffffffffff83166020820152606060408201526000611dc46060830184611bc3565b95945050505050565b604051601f8201601f1916810167ffffffffffffffff81118282101715611df657611df6611f1f565b604052919050565b600067ffffffffffffffff821115611e1857611e18611f1f565b5060051b60200190565b600080821280156001600160ff1b0384900385131615611e4457611e44611f09565b600160ff1b8390038412811615611e5d57611e5d611f09565b50500190565b60008160040b8360040b82821282647fffffffff03821381151615611e8a57611e8a611f09565b82647fffffffff19038212811615611ea457611ea4611f09565b50019392505050565b600081810b83820b82821282607f03821381151615611ece57611ece611f09565b82607f19038212811615611ea457611ea4611f09565b600063ffffffff83811690831681811015611f0157611f01611f09565b039392505050565b634e487b7160e01b600052601160045260246000fd5b634e487b7160e01b600052604160045260246000fd5b6001600160a01b0381168114611f4a57600080fd5b5056fea264697066735822122027163c9c7a018e3f491b10f71ff4861efc506503e9f39bd3fc08dc44e99cd34c64736f6c63430008040033";
    private static IntegrationTestEnv testEnv;
    private static FileId fileId;
    private static ContractId contractId;

    @BeforeAll
    public static void beforeAll() throws Exception {
        testEnv = new IntegrationTestEnv(1);

        @Var var response = new FileCreateTransaction()
            .setKeys(testEnv.operatorKey)
            .execute(testEnv.client);

        fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

        new FileAppendTransaction()
            .setFileId(fileId)
            .setContents(SMART_CONTRACT_BYTECODE)
            .setMaxChunks(10)
            .execute(testEnv.client);

        response = new ContractCreateTransaction()
            .setAdminKey(testEnv.operatorKey)
            .setGas(100000)
            .setConstructorParameters(new ContractFunctionParameters())
            .setBytecodeFileId(fileId)
            .execute(testEnv.client);

        contractId = Objects.requireNonNull(response.getReceipt(testEnv.client).contractId);
    }

    @AfterAll
    public static void afterAll() throws Exception {
        new ContractDeleteTransaction()
            .setTransferAccountId(testEnv.operatorId)
            .setContractId(contractId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new FileDeleteTransaction()
            .setFileId(fileId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close();
    }

    // so we don't get "network is busy" error
    @AfterEach
    public void afterEach() throws InterruptedException {
        Thread.sleep(150);
    }

    @Test
    @DisplayName("Can receive uint8 min value from contract call")
    void canCallContractFunctionUint8Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint8", new ContractFunctionParameters().addUint8((byte) 0x0))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint8(0)).isEqualTo((byte) 0);
    }

    @Test
    @DisplayName("Can receive uint8 max value from contract call")
    void canCallContractFunctionUint8Max() throws Exception {
        int uint8Max = 255;
        byte uint8MaxByte = (byte) uint8Max;

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint8", new ContractFunctionParameters().addUint8(uint8MaxByte))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        var uint8MaxFromResponse = Byte.toUnsignedInt(response.getUint8(0));

        assertThat(uint8MaxFromResponse).isEqualTo(uint8Max);
    }

    @Test
    @DisplayName("Can receive uint16 min value from contract call")
    void canCallContractFunctionUint16Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint16", new ContractFunctionParameters().addUint16(0))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint32(0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Can receive uint16 max value from contract call")
    void canCallContractFunctionUint16Max() throws Exception {
        var uint16Max = "65535";
        int uint16MaxInt = Integer.parseUnsignedInt(uint16Max);

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint16", new ContractFunctionParameters().addUint16(uint16MaxInt))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        var uint16MaxIntFromResponse = Integer.toUnsignedString(response.getUint32(0));

        assertThat(uint16MaxIntFromResponse).isEqualTo(uint16Max);
    }

    @Test
    @DisplayName("Can receive uint24 min value from contract call")
    void canCallContractFunctionUint24Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint24", new ContractFunctionParameters().addUint24(0))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint32(0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Can receive uint24 max value from contract call")
    void canCallContractFunctionUint24Max() throws Exception {
        var uint24Max = "16777215";
        int uint24MaxInt = Integer.parseUnsignedInt(uint24Max);

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint24", new ContractFunctionParameters().addUint24(uint24MaxInt))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        var uint24MaxIntFromResponse = Integer.toUnsignedString(response.getUint32(0));

        assertThat(uint24MaxIntFromResponse).isEqualTo(uint24Max);
    }

    @Test
    @DisplayName("Can receive uint32 min value from contract call")
    void canCallContractFunctionUint32Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint32", new ContractFunctionParameters().addUint32(0))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint32(0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Can receive uint32 max value from contract call")
    void canCallContractFunctionUint32Max() throws Exception {
        var uint32Max = "4294967295";
        int uint32MaxInt = Integer.parseUnsignedInt(uint32Max);

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint32", new ContractFunctionParameters().addUint32(uint32MaxInt))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        var uint32MaxIntFromResponse = Integer.toUnsignedString(response.getUint32(0));

        assertThat(uint32MaxIntFromResponse).isEqualTo(uint32Max);
    }

    @Test
    @DisplayName("Can receive uint40 min value from contract call")
    void canCallContractFunctionUint40Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint40", new ContractFunctionParameters().addUint40(0))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint64(0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Can receive uint40 max value from contract call")
    void canCallContractFunctionUint40Max() throws Exception {
        var uint40Max = "109951162777";
        long uint40MaxLong = Long.parseUnsignedLong(uint40Max);

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint40", new ContractFunctionParameters().addUint40(uint40MaxLong))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        var uint64MaxLongFromResponse = Long.toUnsignedString(response.getUint64(0));

        assertThat(uint64MaxLongFromResponse).isEqualTo(uint40Max);
    }

    @Test
    @DisplayName("Can receive uint48 min value from contract call")
    void canCallContractFunctionUint48Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint48", new ContractFunctionParameters().addUint48(0))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint64(0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Can receive uint48 max value from contract call")
    void canCallContractFunctionUint48Max() throws Exception {
        var uint48Max = "281474976710655";
        long uint48MaxLong = Long.parseUnsignedLong(uint48Max);

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint48", new ContractFunctionParameters().addUint48(uint48MaxLong))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        var uint64MaxLongFromResponse = Long.toUnsignedString(response.getUint64(0));

        assertThat(uint64MaxLongFromResponse).isEqualTo(uint48Max);
    }

    @Test
    @DisplayName("Can receive uint56 min value from contract call")
    void canCallContractFunctionUint56Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint56", new ContractFunctionParameters().addUint56(0))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint64(0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Can receive uint56 max value from contract call")
    void canCallContractFunctionUint56Max() throws Exception {
        var uint56Max = "72057594037927935";
        long uint56MaxLong = Long.parseUnsignedLong(uint56Max);

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint56", new ContractFunctionParameters().addUint56(uint56MaxLong))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        var uint64MaxLongFromResponse = Long.toUnsignedString(response.getUint64(0));

        assertThat(uint64MaxLongFromResponse).isEqualTo(uint56Max);
    }

    @Test
    @DisplayName("Can receive uint64 min value from contract call")
    void canCallContractFunctionUint64Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint64", new ContractFunctionParameters().addUint64(0))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint64(0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Can receive uint64 max value from contract call")
    void canCallContractFunctionUint64Max() throws Exception {
        var uint64Max = "9223372036854775807";
        long uint64MaxLong = Long.parseUnsignedLong(uint64Max);

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint64", new ContractFunctionParameters().addUint64(uint64MaxLong))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        var uint64MaxLongFromResponse = Long.toUnsignedString(response.getUint64(0));

        assertThat(uint64MaxLongFromResponse).isEqualTo(uint64Max);
    }

    @Test
    @DisplayName("Can receive uint72 min value from contract call")
    void canCallContractFunctionUint72Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint72", new ContractFunctionParameters().addUint72(BigInteger.ZERO))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(BigInteger.ZERO);
    }

    @Test
    @DisplayName("Can receive uint72 max value from contract call")
    void canCallContractFunctionUint72Max() throws Exception {
        BigInteger uint72Max = new BigInteger(
            "4722366482869645213695");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint72", new ContractFunctionParameters().addUint72(uint72Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(uint72Max);
    }

    @Test
    @DisplayName("Can receive uint80 min value from contract call")
    void canCallContractFunctionUint80Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint80", new ContractFunctionParameters().addUint80(BigInteger.ZERO))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(BigInteger.ZERO);
    }

    @Test
    @DisplayName("Can receive uint80 max value from contract call")
    void canCallContractFunctionUint80Max() throws Exception {
        BigInteger uint80Max = new BigInteger(
            "1208925819614629174706175");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint80", new ContractFunctionParameters().addUint80(uint80Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(uint80Max);
    }

    @Test
    @DisplayName("Can receive uint88 min value from contract call")
    void canCallContractFunctionUint88Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint88", new ContractFunctionParameters().addUint88(BigInteger.ZERO))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(BigInteger.ZERO);
    }

    @Test
    @DisplayName("Can receive uint88 max value from contract call")
    void canCallContractFunctionUint88Max() throws Exception {
        BigInteger uint88Max = new BigInteger(
            "309485009821345068724781055");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint88", new ContractFunctionParameters().addUint88(uint88Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(uint88Max);
    }

    @Test
    @DisplayName("Can receive uint96 min value from contract call")
    void canCallContractFunctionUint96Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint96", new ContractFunctionParameters().addUint96(BigInteger.ZERO))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(BigInteger.ZERO);
    }

    @Test
    @DisplayName("Can receive uint96 max value from contract call")
    void canCallContractFunctionUint96Max() throws Exception {
        BigInteger uint96Max = new BigInteger(
            "79228162514264337593543950335");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint96", new ContractFunctionParameters().addUint96(uint96Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(uint96Max);
    }

    @Test
    @DisplayName("Can receive uint104 min value from contract call")
    void canCallContractFunctionUint104Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint104", new ContractFunctionParameters().addUint104(BigInteger.ZERO))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(BigInteger.ZERO);
    }

    @Test
    @DisplayName("Can receive uint104 max value from contract call")
    void canCallContractFunctionUint104Max() throws Exception {
        BigInteger uint104Max = new BigInteger(
            "20282409603651670423947251286015");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint104", new ContractFunctionParameters().addUint104(uint104Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(uint104Max);
    }

    @Test
    @DisplayName("Can receive uint112 min value from contract call")
    void canCallContractFunctionUint112Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint112", new ContractFunctionParameters().addUint112(BigInteger.ZERO))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(BigInteger.ZERO);
    }

    @Test
    @DisplayName("Can receive uint112 max value from contract call")
    void canCallContractFunctionUint112Max() throws Exception {
        BigInteger uint112Max = new BigInteger(
            "5192296858534827628530496329220095");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint112", new ContractFunctionParameters().addUint112(uint112Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(uint112Max);
    }

    @Test
    @DisplayName("Can receive uint120 min value from contract call")
    void canCallContractFunctionUint120Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint120", new ContractFunctionParameters().addUint120(BigInteger.ZERO))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(BigInteger.ZERO);
    }

    @Test
    @DisplayName("Can receive uint120 max value from contract call")
    void canCallContractFunctionUint120Max() throws Exception {
        BigInteger uint120Max = new BigInteger(
            "1329227995784915872903807060280344575");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint120", new ContractFunctionParameters().addUint120(uint120Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(uint120Max);
    }

    @Test
    @DisplayName("Can receive uint128 min value from contract call")
    void canCallContractFunctionUint128Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint128", new ContractFunctionParameters().addUint128(BigInteger.ZERO))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(BigInteger.ZERO);
    }

    @Test
    @DisplayName("Can receive uint128 max value from contract call")
    void canCallContractFunctionUint128Max() throws Exception {
        BigInteger uint128Max = new BigInteger(
            "340282366920938463463374607431768211455");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint128", new ContractFunctionParameters().addUint128(uint128Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(uint128Max);
    }

    @Test
    @DisplayName("Can receive uint136 min value from contract call")
    void canCallContractFunctionUint136Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint136", new ContractFunctionParameters().addUint136(BigInteger.ZERO))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(BigInteger.ZERO);
    }

    @Test
    @DisplayName("Can receive uint136 max value from contract call")
    void canCallContractFunctionUint136Max() throws Exception {
        BigInteger uint136Max = new BigInteger(
            "87112285931760246646623899502532662132735");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint136", new ContractFunctionParameters().addUint136(uint136Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(uint136Max);
    }

    @Test
    @DisplayName("Can receive uint144 min value from contract call")
    void canCallContractFunctionUint144Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint144", new ContractFunctionParameters().addUint144(BigInteger.ZERO))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(BigInteger.ZERO);
    }

    @Test
    @DisplayName("Can receive uint144 max value from contract call")
    void canCallContractFunctionUint144Max() throws Exception {
        BigInteger uint144Max = new BigInteger(
            "22300745198530623141535718272648361505980415");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint144", new ContractFunctionParameters().addUint144(uint144Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(uint144Max);
    }

    @Test
    @DisplayName("Can receive uint152 min value from contract call")
    void canCallContractFunctionUint152Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint152", new ContractFunctionParameters().addUint152(BigInteger.ZERO))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(BigInteger.ZERO);
    }

    @Test
    @DisplayName("Can receive uint152 max value from contract call")
    void canCallContractFunctionUint152Max() throws Exception {
        BigInteger uint152Max = new BigInteger(
            "5708990770823839524233143877797980545530986495");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint152", new ContractFunctionParameters().addUint152(uint152Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(uint152Max);
    }

    @Test
    @DisplayName("Can receive uint160 min value from contract call")
    void canCallContractFunctionUint160Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint160", new ContractFunctionParameters().addUint160(BigInteger.ZERO))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(BigInteger.ZERO);
    }

    @Test
    @DisplayName("Can receive uint160 max value from contract call")
    void canCallContractFunctionUint160Max() throws Exception {
        BigInteger uint160Max = new BigInteger(
            "1461501637330902918203684832716283019655932542975");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint160", new ContractFunctionParameters().addUint160(uint160Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(uint160Max);
    }

    @Test
    @DisplayName("Can receive uint168 min value from contract call")
    void canCallContractFunctionUint168Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint168", new ContractFunctionParameters().addUint168(BigInteger.ZERO))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(BigInteger.ZERO);
    }

    @Test
    @DisplayName("Can receive uint168 max value from contract call")
    void canCallContractFunctionUint168Max() throws Exception {
        BigInteger uint168Max = new BigInteger(
            "374144419156711147060143317175368453031918731001855");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint168", new ContractFunctionParameters().addUint168(uint168Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(uint168Max);
    }

    @Test
    @DisplayName("Can receive uint176 min value from contract call")
    void canCallContractFunctionUint176Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint176", new ContractFunctionParameters().addUint176(BigInteger.ZERO))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(BigInteger.ZERO);
    }

    @Test
    @DisplayName("Can receive uint176 max value from contract call")
    void canCallContractFunctionUint176Max() throws Exception {
        BigInteger uint176Max = new BigInteger(
            "95780971304118053647396689196894323976171195136475135");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint176", new ContractFunctionParameters().addUint176(uint176Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(uint176Max);
    }

    @Test
    @DisplayName("Can receive uint184 min value from contract call")
    void canCallContractFunctionUint184Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint184", new ContractFunctionParameters().addUint184(BigInteger.ZERO))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(BigInteger.ZERO);
    }

    @Test
    @DisplayName("Can receive uint184 max value from contract call")
    void canCallContractFunctionUint184Max() throws Exception {
        BigInteger uint184Max = new BigInteger(
            "24519928653854221733733552434404946937899825954937634815");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint184", new ContractFunctionParameters().addUint184(uint184Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(uint184Max);
    }

    @Test
    @DisplayName("Can receive uint192 min value from contract call")
    void canCallContractFunctionUint192Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint192", new ContractFunctionParameters().addUint192(BigInteger.ZERO))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(BigInteger.ZERO);
    }

    @Test
    @DisplayName("Can receive uint192 max value from contract call")
    void canCallContractFunctionUint192Max() throws Exception {
        BigInteger uint192Max = new BigInteger(
            "6277101735386680763835789423207666416102355444464034512895");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint192", new ContractFunctionParameters().addUint192(uint192Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(uint192Max);
    }

    @Test
    @DisplayName("Can receive uint200 min value from contract call")
    void canCallContractFunctionUint200Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint200", new ContractFunctionParameters().addUint200(BigInteger.ZERO))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(BigInteger.ZERO);
    }

    @Test
    @DisplayName("Can receive uint200 max value from contract call")
    void canCallContractFunctionUint200Max() throws Exception {
        BigInteger uint200Max = new BigInteger(
            "1606938044258990275541962092341162602522202993782792835301375");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint200", new ContractFunctionParameters().addUint200(uint200Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(uint200Max);
    }

    @Test
    @DisplayName("Can receive uint208 min value from contract call")
    void canCallContractFunctionUint208Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint208", new ContractFunctionParameters().addUint208(BigInteger.ZERO))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(BigInteger.ZERO);
    }

    @Test
    @DisplayName("Can receive uint208 max value from contract call")
    void canCallContractFunctionUint208Max() throws Exception {
        BigInteger uint208Max = new BigInteger(
            "411376139330301510538742295639337626245683966408394965837152255");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint208", new ContractFunctionParameters().addUint208(uint208Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(uint208Max);
    }

    @Test
    @DisplayName("Can receive uint216 min value from contract call")
    void canCallContractFunctionUint216Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint216", new ContractFunctionParameters().addUint216(BigInteger.ZERO))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(BigInteger.ZERO);
    }

    @Test
    @DisplayName("Can receive uint216 max value from contract call")
    void canCallContractFunctionUint216Max() throws Exception {
        BigInteger uint216Max = new BigInteger(
            "105312291668557186697918027683670432318895095400549111254310977535");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint216", new ContractFunctionParameters().addUint216(uint216Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(uint216Max);
    }

    @Test
    @DisplayName("Can receive uint224 min value from contract call")
    void canCallContractFunctionUint224Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint224", new ContractFunctionParameters().addUint224(BigInteger.ZERO))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(BigInteger.ZERO);
    }

    @Test
    @DisplayName("Can receive uint224 max value from contract call")
    void canCallContractFunctionUint224Max() throws Exception {
        BigInteger uint224Max = new BigInteger(
            "26959946667150639794667015087019630673637144422540572481103610249215");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint224", new ContractFunctionParameters().addUint224(uint224Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(uint224Max);
    }

    @Test
    @DisplayName("Can receive uint232 min value from contract call")
    void canCallContractFunctionUint232Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint232", new ContractFunctionParameters().addUint232(BigInteger.ZERO))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(BigInteger.ZERO);
    }

    @Test
    @DisplayName("Can receive uint232 max value from contract call")
    void canCallContractFunctionUint232Max() throws Exception {
        BigInteger uint232Max = new BigInteger(
            "6901746346790563787434755862277025452451108972170386555162524223799295");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint232", new ContractFunctionParameters().addUint232(uint232Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(uint232Max);
    }

    @Test
    @DisplayName("Can receive uint240 min value from contract call")
    void canCallContractFunctionUint240Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint240", new ContractFunctionParameters().addUint240(BigInteger.ZERO))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(BigInteger.ZERO);
    }

    @Test
    @DisplayName("Can receive uint240 max value from contract call")
    void canCallContractFunctionUint240Max() throws Exception {
        BigInteger uint240Max = new BigInteger(
            "1766847064778384329583297500742918515827483896875618958121606201292619775");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint240", new ContractFunctionParameters().addUint240(uint240Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(uint240Max);
    }

    @Test
    @DisplayName("Can receive uint248 min value from contract call")
    void canCallContractFunctionUint248Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint248", new ContractFunctionParameters().addUint248(BigInteger.ZERO))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(BigInteger.ZERO);
    }

    @Test
    @DisplayName("Can receive uint248 max value from contract call")
    void canCallContractFunctionUint248Max() throws Exception {
        BigInteger uint248Max = new BigInteger(
            "452312848583266388373324160190187140051835877600158453279131187530910662655");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint248", new ContractFunctionParameters().addUint248(uint248Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(uint248Max);
    }

    @Test
    @DisplayName("Can receive uint256 min value from contract call")
    void canCallContractFunctionUint256Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint256", new ContractFunctionParameters().addUint256(BigInteger.ZERO))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(BigInteger.ZERO);
    }

    @Test
    @DisplayName("Can receive uint256 max value from contract call")
    void canCallContractFunctionUint256Max() throws Exception {
        BigInteger uint256Max = new BigInteger("2").pow(256).subtract(BigInteger.ONE);

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint256", new ContractFunctionParameters().addUint256(uint256Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint256(0)).isEqualTo(uint256Max);
    }

    @Test
    @DisplayName("Can receive int8 min value from contract call")
    void canCallContractFunctionInt8Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt8", new ContractFunctionParameters().addInt8(Byte.MIN_VALUE))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt8(0)).isEqualTo(Byte.MIN_VALUE);
    }

    @Test
    @DisplayName("Can receive int8 max value from contract call")
    void canCallContractFunctionInt8Max() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt8", new ContractFunctionParameters().addInt8(Byte.MAX_VALUE))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt8(0)).isEqualTo(Byte.MAX_VALUE);
    }

    @Test
    @DisplayName("Can receive int16 min value from contract call")
    void canCallContractFunctionInt16Min() throws Exception {
        int int16Min = -32768;

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt16", new ContractFunctionParameters().addInt16(int16Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt32(0)).isEqualTo(int16Min);
    }

    @Test
    @DisplayName("Can receive int16 max value from contract call")
    void canCallContractFunctionInt16Max() throws Exception {
        int int16Max = 32767;

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt16", new ContractFunctionParameters().addInt16(int16Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt32(0)).isEqualTo(int16Max);
    }

    @Test
    @DisplayName("Can receive int24 min value from contract call")
    void canCallContractFunctionInt24Min() throws Exception {
        int int24Min = -8388608;

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt24", new ContractFunctionParameters().addInt24(int24Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt32(0)).isEqualTo(int24Min);
    }

    @Test
    @DisplayName("Can receive int24 max value from contract call")
    void canCallContractFunctionInt24Max() throws Exception {
        int int24Max = 8388607;

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt24", new ContractFunctionParameters().addInt24(int24Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt32(0)).isEqualTo(int24Max);
    }

    @Test
    @DisplayName("Can receive int32 min value from contract call")
    void canCallContractFunctionInt32Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt32", new ContractFunctionParameters().addInt32(Integer.MIN_VALUE))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt32(0)).isEqualTo(Integer.MIN_VALUE);
    }

    @Test
    @DisplayName("Can receive int32 max value from contract call")
    void canCallContractFunctionInt32Max() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt32", new ContractFunctionParameters().addInt32(Integer.MAX_VALUE))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt32(0)).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    @DisplayName("Can receive int40 min value from contract call")
    void canCallContractFunctionInt40Min() throws Exception {
        long int40Min = -549755813888L;

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt40", new ContractFunctionParameters().addInt40(int40Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt64(0)).isEqualTo(int40Min);
    }

    @Test
    @DisplayName("Can receive int40 max value from contract call")
    void canCallContractFunctionInt40Max() throws Exception {
        long int40Max = 549755813887L;

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt40", new ContractFunctionParameters().addInt40(int40Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt64(0)).isEqualTo(int40Max);
    }

    @Test
    @DisplayName("Can receive int48 min value from contract call")
    void canCallContractFunctionInt48Min() throws Exception {
        long int48Min = -140737488355328L;

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt48", new ContractFunctionParameters().addInt48(int48Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt64(0)).isEqualTo(int48Min);
    }

    @Test
    @DisplayName("Can receive int48 max value from contract call")
    void canCallContractFunctionInt48Max() throws Exception {
        long int48Max = 140737488355327L;

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt48", new ContractFunctionParameters().addInt48(int48Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt64(0)).isEqualTo(int48Max);
    }

    @Test
    @DisplayName("Can receive int56 min value from contract call")
    void canCallContractFunctionInt56Min() throws Exception {
        long int56Min = -36028797018963968L;

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt56", new ContractFunctionParameters().addInt56(int56Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt64(0)).isEqualTo(int56Min);
    }

    @Test
    @DisplayName("Can receive int56 max value from contract call")
    void canCallContractFunctionInt56Max() throws Exception {
        long int56Max = 36028797018963967L;

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt56", new ContractFunctionParameters().addInt56(int56Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt64(0)).isEqualTo(int56Max);
    }

    @Test
    @DisplayName("Can receive int64 min value from contract call")
    void canCallContractFunctionInt64Min() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt64", new ContractFunctionParameters().addInt64(Long.MIN_VALUE))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint64(0)).isEqualTo(Long.MIN_VALUE);
    }

    @Test
    @DisplayName("Can receive int64 max value from contract call")
    void canCallContractFunctionInt64Max() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnUint64", new ContractFunctionParameters().addUint64(Long.MAX_VALUE))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getUint64(0)).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    @DisplayName("Can receive int72 min value from contract call")
    void canCallContractFunctionInt72Min() throws Exception {
        BigInteger int72Min = new BigInteger(
            "-2361183241434822606848");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt72", new ContractFunctionParameters().addInt72(int72Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int72Min);
    }

    @Test
    @DisplayName("Can receive int72 max value from contract call")
    void canCallContractFunctionInt72Max() throws Exception {
        BigInteger int72Max = new BigInteger(
            "2361183241434822606847");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt72", new ContractFunctionParameters().addInt72(int72Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int72Max);
    }

    @Test
    @DisplayName("Can receive int80 min value from contract call")
    void canCallContractFunctionInt80Min() throws Exception {
        BigInteger int80Min = new BigInteger(
            "-604462909807314587353088");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt80", new ContractFunctionParameters().addInt80(int80Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int80Min);
    }

    @Test
    @DisplayName("Can receive int80 max value from contract call")
    void canCallContractFunctionInt80Max() throws Exception {
        BigInteger int80Max = new BigInteger(
            "604462909807314587353087");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt80", new ContractFunctionParameters().addInt80(int80Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int80Max);
    }

    @Test
    @DisplayName("Can receive int88 min value from contract call")
    void canCallContractFunctionInt88Min() throws Exception {
        BigInteger int88Min = new BigInteger(
            "-154742504910672534362390528");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt88", new ContractFunctionParameters().addInt88(int88Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int88Min);
    }

    @Test
    @DisplayName("Can receive int88 max value from contract call")
    void canCallContractFunctionInt88Max() throws Exception {
        BigInteger int88Max = new BigInteger(
            "154742504910672534362390527");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt88", new ContractFunctionParameters().addInt88(int88Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int88Max);
    }

    @Test
    @DisplayName("Can receive int96 min value from contract call")
    void canCallContractFunctionInt96Min() throws Exception {
        BigInteger int96Min = new BigInteger(
            "-39614081257132168796771975168");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt96", new ContractFunctionParameters().addInt96(int96Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int96Min);
    }

    @Test
    @DisplayName("Can receive int96 max value from contract call")
    void canCallContractFunctionInt96Max() throws Exception {
        BigInteger int96Max = new BigInteger(
            "39614081257132168796771975167");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt96", new ContractFunctionParameters().addInt96(int96Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int96Max);
    }

    @Test
    @DisplayName("Can receive int104 min value from contract call")
    void canCallContractFunctionInt104Min() throws Exception {
        BigInteger int104Min = new BigInteger(
            "-10141204801825835211973625643008");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt104", new ContractFunctionParameters().addInt104(int104Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int104Min);
    }

    @Test
    @DisplayName("Can receive int104 max value from contract call")
    void canCallContractFunctionInt104Max() throws Exception {
        BigInteger int104Max = new BigInteger(
            "10141204801825835211973625643007");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt104", new ContractFunctionParameters().addInt104(int104Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int104Max);
    }

    @Test
    @DisplayName("Can receive int112 min value from contract call")
    void canCallContractFunctionInt112Min() throws Exception {
        BigInteger int112Min = new BigInteger(
            "-2596148429267413814265248164610048");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt112", new ContractFunctionParameters().addInt112(int112Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int112Min);
    }

    @Test
    @DisplayName("Can receive int112 max value from contract call")
    void canCallContractFunctionInt112Max() throws Exception {
        BigInteger int112Max = new BigInteger(
            "2596148429267413814265248164610047");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt112", new ContractFunctionParameters().addInt112(int112Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int112Max);
    }

    @Test
    @DisplayName("Can receive int120 min value from contract call")
    void canCallContractFunctionInt120Min() throws Exception {
        BigInteger int120Min = new BigInteger(
            "-664613997892457936451903530140172288");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt120", new ContractFunctionParameters().addInt120(int120Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int120Min);
    }

    @Test
    @DisplayName("Can receive int120 max value from contract call")
    void canCallContractFunctionInt120Max() throws Exception {
        BigInteger int120Max = new BigInteger(
            "664613997892457936451903530140172287");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt120", new ContractFunctionParameters().addInt120(int120Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int120Max);
    }

    @Test
    @DisplayName("Can receive int128 min value from contract call")
    void canCallContractFunctionInt128Min() throws Exception {
        BigInteger int128Min = new BigInteger(
            "-170141183460469231731687303715884105728");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt128", new ContractFunctionParameters().addInt128(int128Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int128Min);
    }

    @Test
    @DisplayName("Can receive int128 max value from contract call")
    void canCallContractFunctionInt128Max() throws Exception {
        BigInteger int128Max = new BigInteger(
            "170141183460469231731687303715884105727");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt128", new ContractFunctionParameters().addInt128(int128Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int128Max);
    }

    @Test
    @DisplayName("Can receive int136 min value from contract call")
    void canCallContractFunctionInt136Min() throws Exception {
        BigInteger int136Min = new BigInteger(
            "-43556142965880123323311949751266331066368");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt136", new ContractFunctionParameters().addInt136(int136Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int136Min);
    }

    @Test
    @DisplayName("Can receive int136 max value from contract call")
    void canCallContractFunctionInt136Max() throws Exception {
        BigInteger int136Max = new BigInteger(
            "43556142965880123323311949751266331066367");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt136", new ContractFunctionParameters().addInt136(int136Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int136Max);
    }

    @Test
    @DisplayName("Can receive int144 min value from contract call")
    void canCallContractFunctionInt144Min() throws Exception {
        BigInteger int144Min = new BigInteger(
            "-11150372599265311570767859136324180752990208");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt144", new ContractFunctionParameters().addInt144(int144Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int144Min);
    }

    @Test
    @DisplayName("Can receive int144 max value from contract call")
    void canCallContractFunctionInt144Max() throws Exception {
        BigInteger int144Max = new BigInteger(
            "11150372599265311570767859136324180752990207");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt144", new ContractFunctionParameters().addInt144(int144Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int144Max);
    }

    @Test
    @DisplayName("Can receive int152 min value from contract call")
    void canCallContractFunctionInt152Min() throws Exception {
        BigInteger int152Min = new BigInteger(
            "-2854495385411919762116571938898990272765493248");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt152", new ContractFunctionParameters().addInt152(int152Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int152Min);
    }

    @Test
    @DisplayName("Can receive int152 max value from contract call")
    void canCallContractFunctionInt152Max() throws Exception {
        BigInteger int152Max = new BigInteger(
            "2854495385411919762116571938898990272765493247");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt152", new ContractFunctionParameters().addInt152(int152Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int152Max);
    }

    @Test
    @DisplayName("Can receive int160 min value from contract call")
    void canCallContractFunctionInt160Min() throws Exception {
        BigInteger int160Min = new BigInteger(
            "-730750818665451459101842416358141509827966271488");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt160", new ContractFunctionParameters().addInt160(int160Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int160Min);
    }

    @Test
    @DisplayName("Can receive int160 max value from contract call")
    void canCallContractFunctionInt160Max() throws Exception {
        BigInteger int160Max = new BigInteger(
            "730750818665451459101842416358141509827966271487");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt160", new ContractFunctionParameters().addInt160(int160Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int160Max);
    }

    @Test
    @DisplayName("Can receive int168 min value from contract call")
    void canCallContractFunctionInt168Min() throws Exception {
        BigInteger int168Min = new BigInteger(
            "-187072209578355573530071658587684226515959365500928");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt168", new ContractFunctionParameters().addInt168(int168Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int168Min);
    }

    @Test
    @DisplayName("Can receive int168 max value from contract call")
    void canCallContractFunctionInt168Max() throws Exception {
        BigInteger int168Max = new BigInteger(
            "187072209578355573530071658587684226515959365500927");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt168", new ContractFunctionParameters().addInt168(int168Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int168Max);
    }

    @Test
    @DisplayName("Can receive int176 min value from contract call")
    void canCallContractFunctionInt176Min() throws Exception {
        BigInteger int176Min = new BigInteger(
            "-47890485652059026823698344598447161988085597568237568");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt176", new ContractFunctionParameters().addInt176(int176Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int176Min);
    }

    @Test
    @DisplayName("Can receive int176 max value from contract call")
    void canCallContractFunctionInt176Max() throws Exception {
        BigInteger int176Max = new BigInteger(
            "47890485652059026823698344598447161988085597568237567");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt176", new ContractFunctionParameters().addInt176(int176Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int176Max);
    }

    @Test
    @DisplayName("Can receive int184 min value from contract call")
    void canCallContractFunctionInt184Min() throws Exception {
        BigInteger int184Min = new BigInteger(
            "-12259964326927110866866776217202473468949912977468817408");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt184", new ContractFunctionParameters().addInt184(int184Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int184Min);
    }

    @Test
    @DisplayName("Can receive int184 max value from contract call")
    void canCallContractFunctionInt184Max() throws Exception {
        BigInteger int184Max = new BigInteger(
            "12259964326927110866866776217202473468949912977468817407");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt184", new ContractFunctionParameters().addInt184(int184Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int184Max);
    }

    @Test
    @DisplayName("Can receive int192 min value from contract call")
    void canCallContractFunctionInt192Min() throws Exception {
        BigInteger int192Min = new BigInteger(
            "-3138550867693340381917894711603833208051177722232017256448");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt192", new ContractFunctionParameters().addInt192(int192Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int192Min);
    }

    @Test
    @DisplayName("Can receive int192 max value from contract call")
    void canCallContractFunctionInt192Max() throws Exception {
        BigInteger int192Max = new BigInteger(
            "3138550867693340381917894711603833208051177722232017256447");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt192", new ContractFunctionParameters().addInt192(int192Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int192Max);
    }

    @Test
    @DisplayName("Can receive int200 min value from contract call")
    void canCallContractFunctionInt200Min() throws Exception {
        BigInteger int200Min = new BigInteger(
            "-803469022129495137770981046170581301261101496891396417650688");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt200", new ContractFunctionParameters().addInt200(int200Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int200Min);
    }

    @Test
    @DisplayName("Can receive int200 max value from contract call")
    void canCallContractFunctionInt200Max() throws Exception {
        BigInteger int200Max = new BigInteger(
            "803469022129495137770981046170581301261101496891396417650687");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt200", new ContractFunctionParameters().addInt200(int200Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int200Max);
    }

    @Test
    @DisplayName("Can receive int208 min value from contract call")
    void canCallContractFunctionInt208Min() throws Exception {
        BigInteger int208Min = new BigInteger(
            "-205688069665150755269371147819668813122841983204197482918576128");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt208", new ContractFunctionParameters().addInt208(int208Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int208Min);
    }

    @Test
    @DisplayName("Can receive int208 max value from contract call")
    void canCallContractFunctionInt208Max() throws Exception {
        BigInteger int208Max = new BigInteger(
            "205688069665150755269371147819668813122841983204197482918576127");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt208", new ContractFunctionParameters().addInt208(int208Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int208Max);
    }

    @Test
    @DisplayName("Can receive int216 min value from contract call")
    void canCallContractFunctionInt216Min() throws Exception {
        BigInteger int216Min = new BigInteger(
            "-52656145834278593348959013841835216159447547700274555627155488768");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt216", new ContractFunctionParameters().addInt216(int216Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int216Min);
    }

    @Test
    @DisplayName("Can receive int216 max value from contract call")
    void canCallContractFunctionInt216Max() throws Exception {
        BigInteger int216Max = new BigInteger(
            "52656145834278593348959013841835216159447547700274555627155488767");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt216", new ContractFunctionParameters().addInt216(int216Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int216Max);
    }

    @Test
    @DisplayName("Can receive int224 min value from contract call")
    void canCallContractFunctionInt224Min() throws Exception {
        BigInteger int224Min = new BigInteger(
            "-13479973333575319897333507543509815336818572211270286240551805124608");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt224", new ContractFunctionParameters().addInt224(int224Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int224Min);
    }

    @Test
    @DisplayName("Can receive int224 max value from contract call")
    void canCallContractFunctionInt224Max() throws Exception {
        BigInteger int224Max = new BigInteger(
            "13479973333575319897333507543509815336818572211270286240551805124607");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt224", new ContractFunctionParameters().addInt224(int224Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int224Max);
    }

    @Test
    @DisplayName("Can receive int232 min value from contract call")
    void canCallContractFunctionInt232Min() throws Exception {
        BigInteger int232Min = new BigInteger(
            "-3450873173395281893717377931138512726225554486085193277581262111899648");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt232", new ContractFunctionParameters().addInt232(int232Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int232Min);
    }

    @Test
    @DisplayName("Can receive int232 max value from contract call")
    void canCallContractFunctionInt232Max() throws Exception {
        BigInteger int232Max = new BigInteger(
            "3450873173395281893717377931138512726225554486085193277581262111899647");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt232", new ContractFunctionParameters().addInt232(int232Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int232Max);
    }

    @Test
    @DisplayName("Can receive int240 min value from contract call")
    void canCallContractFunctionInt240Min() throws Exception {
        BigInteger int240Min = new BigInteger(
            "-883423532389192164791648750371459257913741948437809479060803100646309888");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt240", new ContractFunctionParameters().addInt240(int240Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int240Min);
    }

    @Test
    @DisplayName("Can receive int240 max value from contract call")
    void canCallContractFunctionInt240Max() throws Exception {
        BigInteger int240Max = new BigInteger(
            "883423532389192164791648750371459257913741948437809479060803100646309887");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt240", new ContractFunctionParameters().addInt240(int240Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int240Max);
    }

    @Test
    @DisplayName("Can receive int248 min value from contract call")
    void canCallContractFunctionInt248Min() throws Exception {
        BigInteger int248Min = new BigInteger(
            "-226156424291633194186662080095093570025917938800079226639565593765455331328");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt248", new ContractFunctionParameters().addInt248(int248Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int248Min);
    }

    @Test
    @DisplayName("Can receive int248 max value from contract call")
    void canCallContractFunctionInt248Max() throws Exception {
        BigInteger int248Max = new BigInteger(
            "226156424291633194186662080095093570025917938800079226639565593765455331327");

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt248", new ContractFunctionParameters().addInt248(int248Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int248Max);
    }

    @Test
    @DisplayName("Can receive int256 min value from contract call")
    void canCallContractFunctionInt256Min() throws Exception {
        BigInteger int256Min = new BigInteger("2").pow(256).divide(BigInteger.TWO).negate();

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt256", new ContractFunctionParameters().addInt256(int256Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int256Min);
    }

    @Test
    @DisplayName("Can receive int256 max value from contract call")
    void canCallContractFunctionInt256Max() throws Exception {
        BigInteger int256Max = new BigInteger("2").pow(256).subtract(BigInteger.ONE).divide(BigInteger.TWO);

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt256", new ContractFunctionParameters().addInt256(int256Max))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int256Max);
    }

    @Test
    @DisplayName("Can receive multiple int8 values from contract call")
    void canCallContractFunctionMultipleInt8() throws Exception {
        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnInt8Multiple", new ContractFunctionParameters().addInt8(Byte.MIN_VALUE))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt8(0)).isEqualTo(Byte.MIN_VALUE);
        assertThat(response.getInt8(1)).isEqualTo((byte) -108);
    }

    @Test
    @DisplayName("Can receive multiple int40 values from contract call")
    void canCallContractFunctionMultipleInt40() throws Exception {
        long int40 = 549755813885L;

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnMultipleInt40", new ContractFunctionParameters().addInt40(int40))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt64(0)).isEqualTo(int40);
        assertThat(response.getInt64(1)).isEqualTo(int40 + 1);
    }

    @Test
    @DisplayName("Can receive multiple int256 values from contract call")
    void canCallContractFunctionMultipleInt256() throws Exception {
        BigInteger int256Min = new BigInteger("2").pow(256).divide(BigInteger.TWO).negate();

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnMultipleInt256", new ContractFunctionParameters().addInt256(int256Min))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getInt256(0)).isEqualTo(int256Min);
        assertThat(response.getInt256(1)).isEqualTo(int256Min.add(BigInteger.ONE));
    }

    @Test
    @DisplayName("Can receive multiple types of values from contract call")
    void canCallContractFunctionMultipleTypes() throws Exception {
        var uint32Max = "4294967295";
        int uint32MaxInt = Integer.parseUnsignedInt(uint32Max);

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnMultipleTypeParams", new ContractFunctionParameters().addUint32(uint32MaxInt))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(Integer.toUnsignedString(response.getUint32(0))).isEqualTo(uint32Max);
        assertThat(response.getUint64(1)).isEqualTo(Long.parseUnsignedLong(uint32Max) - 1);
        assertThat(response.getString(2)).isEqualTo("OK");
    }

    /*
    ContractFunctionParameters#AddInt256BigInt && #AddInt256BigUInt is not implemented (re tests for BigInt256, BigUint256 and MultipleBigInt256)
    */

    @Test
    @DisplayName("Can receive string value from contract call")
    void canCallContractFunctionStringType() throws Exception {
        var testString = "test";

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnString", new ContractFunctionParameters().addString(testString))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getString(0)).isEqualTo(testString);
    }

    @Test
    @DisplayName("Can receive string array value from contract call")
    void canCallContractFunctionStringArrayType() throws Exception {
        var testStringArray = new String[]{"Test1", "Test2"};

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnStringArr", new ContractFunctionParameters().addStringArray(testStringArray))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getStringArray(0).get(0)).isEqualTo(testStringArray[0]);
        assertThat(response.getStringArray(0).get(1)).isEqualTo(testStringArray[1]);
    }

    @Test
    @DisplayName("Can receive address value from contract call")
    void canCallContractFunctionAddressType() throws Exception {
        var testAddress = "1234567890123456789012345678901234567890";

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnAddress", new ContractFunctionParameters().addAddress(testAddress))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getAddress(0)).isEqualTo(testAddress);
    }

    // review this test
    @Test
    @DisplayName("Can receive address value from contract call")
    void canCallContractFunctionAddressArrayType() throws Exception {
        var testAddressArray = new String[]{"1234567890123456789012345678901234567890",
            "1234567890123456789012345678901234567891"};

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnAddressArr", new ContractFunctionParameters().addAddressArray(testAddressArray))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getAddress(2)).isEqualTo(testAddressArray[0]);
        assertThat(response.getAddress(3)).isEqualTo(testAddressArray[1]);
    }

    @Test
    @DisplayName("Can receive boolean value from contract call")
    void canCallContractFunctionBooleanType() throws Exception {
        var testBoolean = true;

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnBoolean", new ContractFunctionParameters().addBool(testBoolean))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getBool(0)).isEqualTo(testBoolean);
    }

    @Test
    @DisplayName("Can receive bytes value from contract call")
    void canCallContractFunctionBytesType() throws Exception {
        var testBytes = "Test".getBytes();

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnBytes", new ContractFunctionParameters().addBytes(testBytes))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getBytes(0)).isEqualTo(testBytes);
    }

    // revisit me -- can't get testBytes[1] from response
    @Test
    @DisplayName("Can receive bytes array value from contract call")
    void canCallContractFunctionBytesArrayType() throws Exception {
        byte[][] testBytes = new byte[][]{"Test1".getBytes(), "Test2".getBytes()};

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnBytesArr", new ContractFunctionParameters().addBytesArray(testBytes))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        // other indices are not contain something meaningful
        assertThat(response.getBytes(3)).isEqualTo(testBytes[0]);
    }

    @Test
    @DisplayName("Can receive bytes32 value from contract call")
    void canCallContractFunctionBytes32Type() throws Exception {
        byte[] testBytes = "Test".getBytes();
        byte[] testBytesLen32 = new byte[32];
        System.arraycopy(testBytes, 0, testBytesLen32, 0, testBytes.length);

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnBytes32", new ContractFunctionParameters().addBytes32(testBytesLen32))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getBytes32(0)).isEqualTo(testBytesLen32);
    }

    // review this test
    @Test
    @DisplayName("Can receive bytes32 array value from contract call")
    void canCallContractFunctionBytes32ArrayType() throws Exception {
        byte[] testBytes = "Test".getBytes();
        byte[] testBytes2 = "Test2".getBytes();
        byte[][] testBytesLen32 = new byte[2][32];
        System.arraycopy(testBytes, 0, testBytesLen32[0], 0, testBytes.length);
        System.arraycopy(testBytes2, 0, testBytesLen32[1], 0, testBytes2.length);

        var response = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(1500000)
            .setFunction("returnBytes32Arr", new ContractFunctionParameters().addBytes32Array(testBytesLen32))
            .setQueryPayment(new Hbar(10))
            .execute(testEnv.client);

        assertThat(response.getBytes32(2)).isEqualTo(testBytesLen32[0]);
        assertThat(response.getBytes32(3)).isEqualTo(testBytesLen32[1]);
    }
}
