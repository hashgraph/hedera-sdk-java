package com.hedera.sdk.file

import com.hedera.sdk.AccountId
import com.hedera.sdk.FileId
import com.hedera.sdk.TransactionId
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant

class FileAppendTransactionTest extends Specification {
	def privateKey = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")

	def "empty builder does not validate"() {
		when:
		new FileAppendTransaction().validate()

		then:
		def e = thrown(IllegalStateException)
		e.message == """transaction builder failed validation:
.setTransactionId() required
.setNodeAccount() required
.setFileId() required
.setContents() required"""
	}

	def "complete builder does validate"() {
		when:
		def txn = new FileAppendTransaction()
				.setNodeAccount(new AccountId(3))
				.setTransactionId(new TransactionId(new AccountId(1234), Instant.parse("2019-04-05T12:00:00Z")))
				.setFileId(new FileId(1, 2, 3))
				.setContents([1, 2, 3, 4] as byte[])
				.testSign(privateKey)

		then:
		notThrown(IllegalStateException)
	}

	def "complete builder serializes"() {
		when:
		def txn = new FileAppendTransaction()
				.setNodeAccount(new AccountId(3))
				.setTransactionId(new TransactionId(new AccountId(1234), Instant.parse("2019-04-05T12:00:00Z")))
				.setFileId(new FileId(1, 2, 3))
				.setContents([1, 2, 3, 4] as byte[])
				.testSign(privateKey).toProto()

		then:
		txn.toString() == """\
body {
  transactionID {
    transactionValidStart {
      seconds: 1554465600
    }
    accountID {
      accountNum: 1234
    }
  }
  nodeAccountID {
    accountNum: 3
  }
  transactionFee: 100000
  transactionValidDuration {
    seconds: 120
  }
  fileAppend {
    fileID {
      shardNum: 1
      realmNum: 2
      fileNum: 3
    }
    contents: "\\001\\002\\003\\004"
  }
}
sigs {
  sigs {
    signatureList {
      sigs {
        ed25519: "i\\366\\002\\236\\202%\\027\\217J=U3\\201\\373\\033\\205P\\315n\\301o\\353\\316\\234A\\353\\315\\265Fh\\275u+\\215W\\002\\230\\\\_\\250<\\306\\342\\262\\212d\\321\\003\\210\\344\\3023@\\3557\\320\\265\\337\\313A\\005\\375l\\000"
      }
    }
  }
}
"""
	}
}
