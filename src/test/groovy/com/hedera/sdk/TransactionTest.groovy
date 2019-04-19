package com.hedera.sdk

import com.hedera.sdk.account.AccountCreateTransaction
import com.hedera.sdk.account.AccountId
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey
import spock.lang.Specification

import java.time.Instant

class TransactionTest extends Specification {
	static final nodeAcctId = new AccountId(0)
	static final acctId = new AccountId(3)
	static final key1 = Ed25519PrivateKey.fromString("302e020100300506032b6570042204203b054fade7a2b0869c6bd4a63b7017cbae7855d12acc357bea718e2c3e805962")

	// new instance for every test
	def txn = new AccountCreateTransaction().with {
		transactionId = new TransactionId(acctId, Instant.parse('2019-04-18T20:50:00Z'))
		nodeAccountId = nodeAcctId
		key = key1.getPublicKey()
		build()
	}

	def 'validate() requires a signature'() {
		when:
		txn.validate()

		then:
		def e = thrown(IllegalStateException)
		e.message == """\
Transaction failed validation:
Transaction requires at least one signature\
"""
	}

	def 'validate() accepts a single signature'() {
		when:
		txn.sign(key1).validate()

		then:
		notThrown(IllegalStateException)
	}

	def 'validate() accepts two signatures'() {
		given:
		def key2 = Ed25519PrivateKey.generate()

		when:
		txn.sign(key1).sign(key2).validate()

		then:
		notThrown(IllegalStateException)
	}

	def 'validate() forbids duplicate signing keys'() {
		when:
		txn.sign(key1).sign(key1)

		then:
		def e = thrown(IllegalArgumentException)
		e.message == "transaction already signed with key: " + key1.toString()
	}

	def 'transaction goes to bytes and back'() {
		when:
		def txn2 = Transaction.fromBytes(null, txn.sign(key1).toBytes())

		then:
		txn.inner.build() == txn2.inner.build()
		txn.nodeAccountId == txn2.nodeAccountId
		txn.transactionId == txn2.transactionId
		noExceptionThrown()
	}
}
