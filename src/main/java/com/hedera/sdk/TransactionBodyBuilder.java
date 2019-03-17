package com.hedera.sdk;

import com.hedera.sdk.proto.TransactionBody;

import javax.annotation.Nonnull;
import java.time.Duration;

abstract class TransactionBodyBuilder<T extends TransactionBodyBuilder<T>> {
  protected TransactionBody.Builder inner = TransactionBody.newBuilder();

  TransactionBodyBuilder() {
    // todo: transaction fees should be defaulted to whatever the transaction fee schedule is
    setTransactionFee(100_000);
    setTransactionValidDuration(Duration.ofSeconds(120));
  }

  /**
   * Sets the account of the node that submits the transaction to the network.
   */
  public T setNodeAccountId(@Nonnull AccountId id) {
    inner.setNodeAccountID(id.inner);

    @SuppressWarnings("unchecked")
    T self = (T) this;
    return self;
  }

  /**
   * Sets the fee that the client pays to execute this transaction, which is
   * split between the network and the node.
   */
  public T setTransactionFee(long fee) {
    inner.setTransactionFee(fee);

    @SuppressWarnings("unchecked")
    T self = (T) this;
    return self;
  }

  /**
   * Sets the the duration that this transaction is valid for. The transaction must
   * consensus before this this elapses.
   */
  public T setTransactionValidDuration(Duration validDuration) {
    inner.setTransactionValidDuration(com.hedera.sdk.proto.Duration.newBuilder().setSeconds(validDuration.getSeconds()).setNanos(validDuration.getNano()));

    @SuppressWarnings("unchecked")
    T self = (T) this;
    return self;
  }

  /**
   * Sets whether the transaction should generate a record. A receipt is always generated but
   * a record is optional.
   */
  public T setGenerateRecord(boolean generateRecord) {
    inner.setGenerateRecord(generateRecord);

    @SuppressWarnings("unchecked")
    T self = (T) this;
    return self;
  }

  /**
   * Sets any notes or description that should be put into the transaction
   * record (if one is requested). Note that a max of length of 100 is enforced.
   */
  public T setMemo(String memo) {
    if (memo.length() > 100) {
      throw new IllegalArgumentException("memo must not be longer than 100 characters");
    }

    inner.setMemo(memo);

    @SuppressWarnings("unchecked")
    T self = (T) this;
    return self;
  }
}
