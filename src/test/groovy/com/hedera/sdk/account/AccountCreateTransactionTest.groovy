package com.hedera.sdk.account

import com.hedera.sdk.AccountId
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
.setNodeAccount() required
.setKey() required"""
	}

	def "Transaction can be built with defaults"() {
		when:
		def tx = new AccountCreateTransaction()

		then:
		tx.toProto().toString() == """\
body {
  transactionFee: 100000
  transactionValidDuration {
    seconds: 120
  }
  cryptoCreateAccount {
    sendRecordThreshold: 9223372036854775807
    receiveRecordThreshold: 9223372036854775807
  }
}
"""
	}

	def "Transaction can be built"() {
		when:
		def now = Instant.ofEpochSecond(1554158542)
		def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new AccountCreateTransaction().with(true, {
			setNodeAccount(new AccountId(3))
			setTransactionId(new TransactionId(new AccountId(1234), Instant.parse("2019-04-08T07:04:00Z")))
			setKey(key.getPublicKey())
			transactionId = txId
			initialBalance = 450
			proxyAccount = new AccountId(1020)
			proxyFraction = 10
			maxReceiveProxyFraction = 20
			receiverSignatureRequired = true
		}).testSign(key)

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
    proxyFraction: 10
    maxReceiveProxyFraction: 20
    sendRecordThreshold: 9223372036854775807
    receiveRecordThreshold: 9223372036854775807
    receiverSigRequired: true
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
        ed25519: "\\216\\032iU\\201M\\207\\f\\352q\\252\\345?\\005M\\243bl\\246\\204\\310\$D\\375g00\\251q[\\3036\\262\\261c\\220\\250\\030\\245\\200\\016\\020/*4\\201\\v\\244\\261\\307\\031\\311#\\257\\272f\\250\\335\\031:C\\242\\320\\002"
      }
    }
  }
}
"""
	}
}
