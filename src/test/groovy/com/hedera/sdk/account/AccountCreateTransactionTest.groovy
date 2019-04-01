package com.hedera.sdk.account

import com.hedera.sdk.AccountId
import com.hedera.sdk.TransactionId
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant

class AccountCreateTransactionTest extends Specification {
	def "Transaction can be built with defaults"() {
		when:
		def tx = new AccountCreateTransaction()

		then:
		tx.build().toString() == """body {
  transactionFee: 100000
  transactionValidDuration {
    seconds: 120
  }
  cryptoCreateAccount {
    sendRecordThreshold: 9223372036854775807
    receiveRecordThreshold: 9223372036854775807
    autoRenewPeriod {
      seconds: 2592000
    }
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
			setKey(key.getPublicKey())
			transactionId = txId
			initialBalance = 450
			proxyAccount = new AccountId(1020)
			proxyFraction = 10
			maxReceiveProxyFraction = 20
			receiverSignatureRequired = true
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
        ed25519: "\\2070t\\316Op\\245\\322\\243+\\213\\313\\211\\215\\331G\\255\\200z2\\036\\256\\325[f\\353\\312\\371aN\\367\\017\\260N\\b\\241#\\275BctK`\\333o\\334D\\177\\207+D\\002H\\250P\\347\\023YFMW\\215I\\002"
      }
    }
  }
}
"""
	}
}
