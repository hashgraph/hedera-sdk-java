package com.hedera.sdk

import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant

class AdminUndeleteTransactionTest extends Specification {
	def "Validation fails with empty builder"() {
		when:
		new AdminUndeleteTransaction().validate()

		then:
		def e = thrown(IllegalStateException)
		e.message == """\
transaction builder failed validation:
.setTransactionId() required
.setNodeAccount() required
.setID() required"""
	}

	def "Transaction can be built with FileId"() {
		when:
		def now = Instant.ofEpochSecond(1554158542)
		def key = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")
		def txId = new TransactionId(new AccountId(2), now)
		def tx = new AdminUndeleteTransaction().with(true, {
			nodeAccount = new AccountId(3)
			transactionId = txId
			ID = new FileId(1, 2, 3)
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
  adminUndelete {
    fileID {
      shardNum: 1
      realmNum: 2
      fileNum: 3
    }
  }
}
sigs {
  sigs {
    signatureList {
      sigs {
        ed25519: "<\\233k\\333)\\307%|\\325#\\365\\b\\253\\016@\\377\\371\\004\\312\\266\\022\\266\\001\\2449\\373\\310r\\251\\372\\2347\\270\\225\\342\\221y\\3533\\325\\377\\206\\361\\333\$E\\304\\026yl\\357\\371\\030\\342\\220D\\226#Y2\\027\\002R\\n"
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
		def tx = new AdminUndeleteTransaction().with(true, {
			nodeAccount = new AccountId(3)
			transactionId = txId
			ID = new ContractId(1, 2, 3)
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
  adminUndelete {
    contractID {
      shardNum: 1
      realmNum: 2
      contractNum: 3
    }
  }
}
sigs {
  sigs {
    signatureList {
      sigs {
        ed25519: "\\020;db\\032\\271\\374\\370(9\\326\\002}\\321\\342\\271\\2375\\372\\374\\366\\334N\\270eA\\271\\247*\\217\\037A\\r\\377\\003.\\352Y\\265\$^\\353ZTd\\376\\036\\235\\315\\330\\0335Ya-\\bn\\021?e\\237\\rX\\v"
      }
    }
  }
}
"""
	}
}
