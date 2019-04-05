package com.hedera.sdk.contract

import com.hedera.sdk.TransactionId;
import com.hedera.sdk.ContractId;
import com.hedera.sdk.FileId;
import com.hedera.sdk.AccountId;
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant
import java.time.Duration

class ContractUpdateTransactionTest extends Specification {
	def "Transaction can be built with defaults"() {
		when:
		def tx = new ContractUpdateTransaction()

		then:
		tx.build().toString() == """body {
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
		def adminKey = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a66b7017cbae7859d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new ContractUpdateTransaction().with(true, {
			transactionId = txId
			contract = new ContractId(1, 2, 3)
			expirationTime = Instant.ofEpochSecond(1554158571)
			//			setAdminKey(adminKey)
			proxyAccount = new AccountId(10, 11, 12)
			autoRenewPeriod = Duration.ofHours(10)
			file = new FileId(4, 5, 6)
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
  contractUpdateInstance {
    contractID {
      shardNum: 1
      realmNum: 2
      contractNum: 3
    }
    expirationTime {
      seconds: 1554158571
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
        ed25519: "*z\\375\\370\\246C\\350\\360b`.\\247\\b\\212\\321\\0000\\257\\031/\\374\\257\\2154\\357\\031\\377\\225o\\205\\305Hs\\004\\206&\\216\\227\\220\\1772\\034A\\301_\\327\\311\\206\\305\\352s\\211\\323P\\261]P=\\210[\\360\\221v\\f"
      }
    }
  }
}
"""
	}
}
