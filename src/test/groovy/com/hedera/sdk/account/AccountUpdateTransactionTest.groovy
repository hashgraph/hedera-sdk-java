package com.hedera.sdk.account

import com.hedera.sdk.AccountId
import com.hedera.sdk.TransactionId
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Duration
import java.time.Instant

class AccountUpdateTransactionTest extends Specification {
	def "Transaction can be built with defaults"() {
		when:
		def tx = new AccountUpdateTransaction()

		then:
		tx.build().toString() == """body {
  transactionFee: 100000
  transactionValidDuration {
    seconds: 120
  }
  cryptoUpdateAccount {
  }
}
"""
	}

	def "Transaction can be built"() {
		when:
		def now = Instant.ofEpochSecond(1554158542)
		def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new AccountUpdateTransaction().with(true, {
			setKey(key.getPublicKey())
			transactionId = txId
			accountForUpdate = new AccountId(2)
			proxyAccount = new AccountId(3)
			proxyFraction = 4
			sendRecordThreshold = 5
			receiveRecordThreshold = 6
			autoRenewPeriod = Duration.ofHours(10)
			expirationTime = Instant.ofEpochSecond(1554158543)
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
  cryptoUpdateAccount {
    accountIDToUpdate {
      accountNum: 2
    }
    key {
      ed25519: "\\344\\361\\300\\353L}\\315\\303\\347\\353\\021p\\263\\b\\212=\\022\\242\\227\\364\\243\\353\\342\\362\\205\\003\\375g5F\\355\\216"
    }
    proxyAccountID {
      accountNum: 3
    }
    proxyFraction: 4
    sendRecordThreshold: 5
    receiveRecordThreshold: 6
    autoRenewPeriod {
      seconds: 36000
    }
    expirationTime {
      seconds: 1554158543
    }
  }
}
sigs {
  sigs {
    signatureList {
      sigs {
        ed25519: "\\027n\\213L\\230IiJ\\333\\000\\244J\\2368\\016\\001\\0065J\\244\\006>\\005Et\\314\\004\\363*\\373K\\335z\\317\\177 \\016\\374\\341\\335\\\\\\241\\252\\267(S\\207\\0168\\211\\312\\341\\310\\360\\275hr\\n\\222;\\3467\\327\\005"
      }
    }
  }
}
"""
	}
}
