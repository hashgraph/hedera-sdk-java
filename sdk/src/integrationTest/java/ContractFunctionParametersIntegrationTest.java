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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Check Go & JS examples for workaround re not implemented funcs
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

    /*
    ContractFunctionResult#getUint16 is not implemented

    ContractFunctionResult#getUint24 is not implemented
    */

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

    /*
    ContractFunctionResult#getUint40 is not implemented

    ContractFunctionResult#getUint48 is not implemented

    ContractFunctionResult#getUint56 is not implemented
    */

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

    /*
    ContractFunctionResult#getUint72 is not implemented

    ContractFunctionResult#getUint80 is not implemented

    ContractFunctionResult#getUint88 is not implemented

    ContractFunctionResult#getUint96 is not implemented

    ContractFunctionResult#getUint104 is not implemented

    ContractFunctionResult#getUint112 is not implemented

    ContractFunctionResult#getUint120 is not implemented

    ContractFunctionResult#getUint128 is not implemented

    ContractFunctionResult#getUint136 is not implemented

    ContractFunctionResult#getUint144 is not implemented

    ContractFunctionResult#getUint152 is not implemented

    ContractFunctionResult#getUint160 is not implemented

    ContractFunctionResult#getUint168 is not implemented

    ContractFunctionResult#getUint176 is not implemented

    ContractFunctionResult#getUint184 is not implemented

    ContractFunctionResult#getUint192 is not implemented

    ContractFunctionResult#getUint200 is not implemented

    ContractFunctionResult#getUint208 is not implemented

    ContractFunctionResult#getUint216 is not implemented

    ContractFunctionResult#getUint224 is not implemented

    ContractFunctionResult#getUint232 is not implemented

    ContractFunctionResult#getUint240 is not implemented

    ContractFunctionResult#getUint248 is not implemented
    */

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

    /*
    ContractFunctionResult#getInt16 is not implemented

    ContractFunctionResult#getInt24 is not implemented
     */

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

    /*
    ContractFunctionResult#getInt40 is not implemented

    ContractFunctionResult#getInt48 is not implemented

    ContractFunctionResult#getInt56 is not implemented
    */

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

    /*
    ContractFunctionResult#getInt72 is not implemented

    ContractFunctionResult#getInt80 is not implemented

    ContractFunctionResult#getInt88 is not implemented

    ContractFunctionResult#getInt96 is not implemented

    ContractFunctionResult#getInt104 is not implemented

    ContractFunctionResult#getInt112 is not implemented

    ContractFunctionResult#getInt120 is not implemented

    ContractFunctionResult#getInt128 is not implemented

    ContractFunctionResult#getInt136 is not implemented

    ContractFunctionResult#getInt144 is not implemented

    ContractFunctionResult#getInt152 is not implemented

    ContractFunctionResult#getInt160 is not implemented

    ContractFunctionResult#getInt168 is not implemented

    ContractFunctionResult#getInt176 is not implemented

    ContractFunctionResult#getInt184 is not implemented

    ContractFunctionResult#getInt192 is not implemented

    ContractFunctionResult#getInt200 is not implemented

    ContractFunctionResult#getInt208 is not implemented

    ContractFunctionResult#getInt216 is not implemented

    ContractFunctionResult#getInt224 is not implemented

    ContractFunctionResult#getInt232 is not implemented

    ContractFunctionResult#getInt240 is not implemented

    ContractFunctionResult#getInt248 is not implemented
    */

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

    // How getX by valIndex works?

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

    /*
    ContractFunctionResult#getInt40 is not implemented
     */

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
    ContractFunctionResult#GetBigInt is not implemented (re tests for BigInt256, BigUint256 and MultipleBigInt256)
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
