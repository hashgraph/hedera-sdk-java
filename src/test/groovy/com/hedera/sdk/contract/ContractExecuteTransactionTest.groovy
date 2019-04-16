package com.hedera.sdk.contract

import com.hedera.sdk.TransactionId
import com.hedera.sdk.ContractId
import com.hedera.sdk.AccountId
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant

class ContractExecuteTransactionTest extends Specification {
	def "Empty builder fails validation"() {
		when:
		new ContractExecuteTransaction().validate()

		then:
		def e = thrown(IllegalStateException)
		e.message == """\
transaction builder failed validation:
.setTransactionId() required
.setNodeAccountId() required
.setContract() required"""
	}

	def "Transaction can be built"() {
		when:
		def now = Instant.ofEpochSecond(1554158542)
		def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new ContractExecuteTransaction().with(true, {
			nodeAccountId = new AccountId(3)
			transactionId = txId
			contract = new ContractId(1, 2, 3)
			gas = 10
			amount = 1000
			functionParameters = [424, 243, 141]
		}).testSign(key).toProto()

		then:
		tx.toString() == """\
body {
  transactionID {
    transactionValidStart {
      seconds: 1554158542
    }
    accountID {
      accountNum: 2
    }
  }
  nodeAccountID {
    accountNum: 3
  }
  transactionFee: 100000
  transactionValidDuration {
    seconds: 120
  }
  contractCall {
    contractID {
      shardNum: 1
      realmNum: 2
      contractNum: 3
    }
    gas: 10
    amount: 1000
    functionParameters: "\\250\\363\\215"
  }
}
sigs {
  sigs {
    signatureList {
      sigs {
        ed25519: "z\\267f\\023/[6xg\\020\\246>\\356\\f\\002h4V\\360\\243q\\a\\343OV\\0052P\\035\$\\355\\177\\344\\023\\271!\\352\\3661\\370a\\337\\362\\244\\2024\\253{o\\002n\\024f\\371\\266\\ar\\375\\206\\253&\\244\\257\\003"
      }
    }
  }
}
"""
	}
}
