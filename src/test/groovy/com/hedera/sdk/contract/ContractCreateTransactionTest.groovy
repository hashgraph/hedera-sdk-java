package com.hedera.sdk.contract

import com.hedera.sdk.TransactionId;
import com.hedera.sdk.FileId;
import com.hedera.sdk.AccountId;
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant
import java.time.Duration

class ContractCreateTransactionTest extends Specification {
	def "Transaction can be built with defaults"() {
		when:
		def tx = new ContractCreateTransaction()

		then:
		tx.build().toString() == """body {
  transactionFee: 100000
  transactionValidDuration {
    seconds: 120
  }
  contractCreateInstance {
  }
}
"""
	}

	def "Transaction can be built"() {
		when:
		def now = Instant.ofEpochSecond(1554158542)
		def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new ContractCreateTransaction().with(true, {
			transactionId = txId
			bytecodeFile = new FileId(1, 2, 3)
			adminKey = key.getPublicKey()
			gas = 10
			initialBalance = 1000
			proxyAccount = new AccountId(4)
			proxyFraction = 5
			autoRenewPeriod = Duration.ofHours(7)
			constructorParams = [10, 11, 12, 13, 425]
			shard = 20
			realm = 40
			newRealmAdminKey = key.getPublicKey()
		}).sign(key)

		then:
		tx.build().toString() == """body {
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
  contractCreateInstance {
    fileID {
      shardNum: 1
      realmNum: 2
      fileNum: 3
    }
    adminKey {
      ed25519: "\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216"
    }
    gas: 10
    initialBalance: 1000
    proxyAccountID {
      accountNum: 4
    }
    proxyFraction: 5
    autoRenewPeriod {
      seconds: 25200
    }
    constructorParameters: "\\n\\v\\f\\r\\251"
    shardID {
      shardNum: 20
    }
    realmID {
      realmNum: 40
    }
    newRealmAdminKey {
      ed25519: "\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216"
    }
  }
}
sigs {
  sigs {
    signatureList {
      sigs {
        ed25519: "\\310\\323\\376\\346t\\364\\320\\201O<P\\312\\317/:\\320\\347\\vp\\312\\375\\037\\355\\b\\034\\242\\350\\317;\\225-\\3273\\023\\0263\\031s^!\\031\\325\\r\\370T\\214\\241`\\353\\257`\\345g L\\252\\316x\\261A5d5\\r"
      }
    }
  }
}
"""
	}
}
