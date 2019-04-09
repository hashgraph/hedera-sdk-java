package com.hedera.sdk.contract

import com.hedera.sdk.TransactionId
import com.hedera.sdk.ContractId
import com.hedera.sdk.FileId
import com.hedera.sdk.AccountId
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant
import java.time.Duration

class ContractUpdateTransactionTest extends Specification {
	def "Transaction can be built with defaults"() {
		when:
		def tx = new ContractUpdateTransaction()

		then:
		tx.toProto().toString() == """body {
  transactionFee: 100000
  transactionValidDuration {
    seconds: 120
  }
  contractUpdateInstance {
  }
}
"""
	}

	// FIXME: Can't set adminKey
	def "Transaction can be built"() {
		when:
		def now = Instant.ofEpochSecond(1554158542)
		def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new ContractUpdateTransaction().with(true, {
			transactionId = txId
			contract = new ContractId(1, 2, 3)
			expirationTime = Instant.ofEpochSecond(1554158571)
			adminKey = key.getPublicKey()
			proxyAccount = new AccountId(10, 11, 12)
			autoRenewPeriod = Duration.ofHours(10)
			file = new FileId(4, 5, 6)
		}).testSign(key)

		then:
		tx.toProto().toString() == """body {
  transactionID {
    transactionValidStart {
      seconds: 1554158542
    }
    accountID {
      accountNum: 2
    }
  }
  transactionFee: 100000
  transactionValidDuration {
    seconds: 120
  }
  contractUpdateInstance {
    contractID {
      shardNum: 1
      realmNum: 2
      contractNum: 3
    }
    expirationTime {
      seconds: 1554158571
    }
    adminKey {
      ed25519: "\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216"
    }
    proxyAccountID {
      shardNum: 10
      realmNum: 11
      accountNum: 12
    }
    autoRenewPeriod {
      seconds: 36000
    }
    fileID {
      shardNum: 4
      realmNum: 5
      fileNum: 6
    }
  }
}
sigs {
  sigs {
    signatureList {
      sigs {
        ed25519: "G\\177e\\000\\3503\\033\\333*\\242B\\352`\\004KeG\\371\\023\\001%=\\000k\\207\\212\\256\\362\\271\\372\\203 f\\256\\003h\\352:\\354\\022rR\\362\\373tM+U\\367\\301U\\023~\\376\\267\\363\\2104,\\n\\344?\\033\\000"
      }
    }
  }
}
"""
	}
}
