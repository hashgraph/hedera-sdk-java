package com.hedera.sdk

import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant

class AdminDeleteTransactionTest extends Specification {
	def "Transaction can be built with defaults"() {
		when:
		def tx = new AdminDeleteTransaction()

		then:
		tx.inner.toString() == """body {
  transactionFee: 100000
  transactionValidDuration {
    seconds: 120
  }
  adminDelete {
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
			nodeAccountId = new AccountId(3)
			transactionId = txId
			ID = new FileId(1, 2, 3)
			expirationTime = Instant.ofEpochSecond(1554158643)
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
        ed25519: "N\\273\\261\\303\\210\\025\\253\\325\\037G+D\\201\\272VD\\301\\003\\214y_\\267/\\353\\201\\\\\\320\\177\\314\\340\\271b\\365C\\222^\\251X\\0041\\374\\236\\021(\\366\\362\\212\\276}M\\016\\201*\\327pX\\222\\v\\222\\037\\301\\2641\\000"
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
			nodeAccountId = new AccountId(3)
			transactionId = txId
			ID = new ContractId(1, 2, 3)
			expirationTime = Instant.ofEpochSecond(1554158643)
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
        ed25519: "\\3032\\257:>\\031\\232w\\a\\t\\335\\357iO\\301SP\\311iE\\336dM\\330\\262cQ\\347Uj\\270\\376UB\\310\\247\\334\$\\n\\335\\327\\316\\034\\357\\241\\251\\227|\\370\\222\\205\\243\\206X\\336;\\311\\rrR\\224a@\\n"
      }
    }
  }
}
"""
	}
}
