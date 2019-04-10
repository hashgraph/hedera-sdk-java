package com.hedera.sdk.contract

import com.hedera.sdk.TransactionId
import com.hedera.sdk.FileId
import com.hedera.sdk.AccountId
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant
import java.time.Duration

class ContractCreateTransactionTest extends Specification {
	def "Empty builder fails validation"() {
		when:
		new ContractCreateTransaction().validate()

		then:
		def e = thrown(IllegalStateException)
		e.message == """\
transaction builder failed validation:
.setTransactionId() required
.setNodeAccount() required
.setBytecodeFile() required"""
	}

	def "Transaction can be built"() {
		when:
		def now = Instant.ofEpochSecond(1554158542)
		def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new ContractCreateTransaction().with(true, {
			nodeAccount = new AccountId(3)
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
        ed25519: "\\235\\3138sY\\215\\000\\322\\251\\372\\245\\320f\\276\\304\\234\\274\\261\\006\\341{^w\\327\\230\\3537\\326|\\357~\\242m\\313\\376<\\023\\031\\252\\345`m\\346\\362&\\265\\'+\\371\\230\\357\\330#;\\a\\200F\\214\\374\\330\\266\\363S\\016"
      }
    }
  }
}
"""
	}
}
