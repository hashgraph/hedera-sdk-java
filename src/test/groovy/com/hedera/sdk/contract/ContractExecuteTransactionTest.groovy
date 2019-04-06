package com.hedera.sdk.contract

import com.hedera.sdk.TransactionId
import com.hedera.sdk.ContractId
import com.hedera.sdk.AccountId
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant

class ContractExecuteTransactionTest extends Specification {
	def "Transaction can be built with defaults"() {
		when:
		def tx = new ContractExecuteTransaction()

		then:
		tx.build().toString() == """body {
  transactionFee: 100000
  transactionValidDuration {
    seconds: 120
  }
  contractCall {
  }
}
"""
	}

	def "Transaction can be built"() {
		when:
		def now = Instant.ofEpochSecond(1554158542)
		def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new ContractExecuteTransaction().with(true, {
			transactionId = txId
			contract = new ContractId(1, 2, 3)
			gas = 10
			amount = 1000
			functionParameters = [424, 243, 141]
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
        ed25519: ":\\017\\311\\034^\\347\\250\\324\\257\\352\\024\\207\\355]\\230`\\263\\332\\331\\363xl\\341*\\223\\0172\\310z\\330\\037s\\316\\221/\\b\\3237\\2015rA\\352~\\2224&\\265\\260\\2144\\271_\\2337\\030\\221\\306m-8\\343^\\a"
      }
    }
  }
}
"""
	}
}
