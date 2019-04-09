package com.hedera.sdk.file

import spock.lang.Specification
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import com.hedera.sdk.FileId
import com.hedera.sdk.AccountId
import com.hedera.sdk.TransactionId
import java.time.Instant

class FileDeleteTransactionTest extends Specification {
	def "Transaction can be built with defaults"() {
		when:
		def tx = new FileDeleteTransaction()
		then:
		tx.build().toString() == """body {
  transactionFee: 100000
  transactionValidDuration {
    seconds: 120
  }
  fileDelete {
  }
}
"""
	}
	def "Transaction can be built"() {
		when:
		def now = Instant.ofEpochSecond(1554158542)
		def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new FileDeleteTransaction().with(true, {
			transactionId = txId
			fileId = new FileId(848, 973, 1234)
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
  fileDelete {
    fileID {
      shardNum: 848
      realmNum: 973
      fileNum: 1234
    }
  }
}
sigs {
  sigs {
    signatureList {
      sigs {
        ed25519: "\\024\\325\\274c\\335\\211j\\232\\322\\021V\\367H\\321{\\3360\\0218D\\vQ\\032\\231\\027\\246\\264I\\306`\\223tM\\213Pr7FAG\\3574i\\025\\241\\b*\\217\\203\\v\\0062,\\247\\277\\252k\\222\\027=\\234\\003\\327\\002"
      }
    }
  }
}
"""
	}
}
