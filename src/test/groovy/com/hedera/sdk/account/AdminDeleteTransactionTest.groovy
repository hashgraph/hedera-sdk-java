package com.hedera.sdk

import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant

class AdminDeleteTransactionTest extends Specification {
	def "Transaction can be built with defaults"() {
		when:
		def tx = new AdminDeleteTransaction()

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

	def "Transaction can be built with FileId"() {
		when:
		def now = Instant.ofEpochSecond(1554158542)
		def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new AdminDeleteTransaction().with(true, {
			transactionId = txId
            ID = new FileId(1, 2, 3)
            expirationTime = Instant.ofEpochSecond(1554158643)
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
  adminDelete {
    fileID {
      shardNum: 1
      realmNum: 2
      fileNum: 3
    }
    expirationTime {
      seconds: 1554158643
    }
  }
}
sigs {
  sigs {
    signatureList {
      sigs {
        ed25519: "\\236\\027\\374\\'E6w\\331+\\376\\325n\\n\\264AQ2\\244\\324\\213\\224N-yT1\\026\\026\\257\\322\\375>;\\262\\252\\035@mV\\204\\353l\\021\\365\\207\\366\\303\\232A\\317F\\006\\372-\\320\\353\\245\\322\\225\\253F>\\312\\b"
      }
    }
  }
}
"""
    }

	def "Transaction can be built with ContractId"() {
		when:
		def now = Instant.ofEpochSecond(1554158542)
		def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new AdminDeleteTransaction().with(true, {
			transactionId = txId
            ID = new ContractId(1, 2, 3)
            expirationTime = Instant.ofEpochSecond(1554158643)
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
  adminDelete {
    contractID {
      shardNum: 1
      realmNum: 2
      contractNum: 3
    }
    expirationTime {
      seconds: 1554158643
    }
  }
}
sigs {
  sigs {
    signatureList {
      sigs {
        ed25519: "\\237h\\237\\325\\277\\251\\361B\\241i\\177eB\\352w\\3156/\\245\\376a2\\223\\235e\\032\\344\\270\\257\\364*D\\322di\\213\\316/h\\324\\bR\\306\\305\\202\\266\\364\\340)\\263Dl\\304\\025\\350\\344\\341\\320\\372}\\213Z\\335\\a"
      }
    }
  }
}
"""
    }
}
