package com.hedera.sdk;

import com.hedera.sdk.proto.TransactionBody;
import java.time.Duration;
import javax.annotation.Nonnull;

abstract class TransactionBodyBuilder<T extends TransactionBodyBuilder<T>> {
  protected transient TransactionBody.Builder inner = TransactionBody.newBuilder();

  private static final int MAX_MEMO_LENGTH = 100;

  TransactionBodyBuilder() {
    // todo: transaction fees should be defaulted to whatever the transaction fee schedule is
    setTransactionFee(100_000);
    setTransactionValidDuration(Duration.ofMinutes(2));
  }

  /**
   * Sets the ID for this transaction, which includes the payer's account (the account paying the
   * transaction fee). If two transactions have the same transactionID, they won't both have an
   * effect.
   */
  public T setTransactionId(@Nonnull TransactionId transactionId) {
    inner.setTransactionID(transactionId.inner);
    return self();
  }

  /** Sets the account of the node that submits the transaction to the network. */
  public final T setNodeAccountId(@Nonnull AccountId accountId) {
    inner.setNodeAccountID(accountId.inner);
    return self();
  }

  /**
   * Sets the fee that the client pays to execute this transaction, which is split between the
   * network and the node.
   */
  public final T setTransactionFee(long fee) {
    inner.setTransactionFee(fee);
    return self();
  }

  /**
   * Sets the the duration that this transaction is valid for. The transaction must consensus before
   * this this elapses.
   */
  public final T setTransactionValidDuration(Duration validDuration) {
    inner.setTransactionValidDuration(
        com.hedera.sdk.proto.Duration.newBuilder()
            .setSeconds(validDuration.getSeconds())
            .setNanos(validDuration.getNano()));

    return self();
  }

  /**
   * Sets whether the transaction should generate a record. A receipt is always generated but a
   * record is optional.
   */
  public final T setGenerateRecord(boolean generateRecord) {
    inner.setGenerateRecord(generateRecord);
    return self();
  }

  /**
   * Sets any notes or description that should be put into the transaction record (if one is
   * requested). Note that a max of length of 100 is enforced.
   */
  public final T setMemo(String memo) {
    if (memo.length() > MAX_MEMO_LENGTH) {
      throw new IllegalArgumentException("memo must not be longer than 100 characters");
    }

    inner.setMemo(memo);
    return self();
  }

  public TransactionBody build() {
    return inner.build();
  }

  // Work around for java not recognized that this is completely safe
  // as T is required to extend this
  @SuppressWarnings("unchecked")
  private T self() {
    return (T) this;
  }
}
