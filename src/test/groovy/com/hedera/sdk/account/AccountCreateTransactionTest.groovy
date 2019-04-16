package com.hedera.sdk.account


import com.hedera.sdk.TransactionId
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant

class AccountCreateTransactionTest extends Specification {
	def "Empty builder fails validation"() {
		when:
		new AccountCreateTransaction().validate()

		then:
		def e = thrown(IllegalStateException)
		e.message == """\
transaction builder failed validation:
.setTransactionId() required
.setNodeAccountId() required
.setKey() required"""
	}

	def "Transaction can be built"() {
		when:
		def now = Instant.ofEpochSecond(1554158542)
		def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new AccountCreateTransaction().with(true, {
			setNodeAccountId(new AccountId(3))
			setTransactionId(new TransactionId(new AccountId(1234), Instant.parse("2019-04-08T07:04:00Z")))
			setKey(key.getPublicKey())
			transactionId = txId
			initialBalance = 450
			proxyAccountId = new AccountId(1020)
			receiverSignatureRequired = true
		}).sign(key)

		then:
		tx.toProto().toString() == """\
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
  cryptoCreateAccount {
    key {
      ed25519: "\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216"
    }
    initialBalance: 450
    proxyAccountID {
      accountNum: 1020
    }
    sendRecordThreshold: 9223372036854775807
    receiveRecordThreshold: 9223372036854775807
    receiverSigRequired: true
    autoRenewPeriod {
      seconds: 2592000
    }
    shardID {
    }
    realmID {
    }
  }
}
sigs {
  sigs {
    signatureList {
      sigs {
        ed25519: "\\377V\\005\\354I\\346\\303\\366\\306\\342\\366\\246a\\257\\235^\\302qII\\227\\220%S\\204\\367/\\207^\\373s\\215rkU\\235\\260z\\363\\355\\254\\353Rb\\255\\315c9\\n\\357\\225\\221\\263bQ\\r\\360(G\\335\\017\\232\\260\\005"
      }
    }
  }
}
"""
	}
}
