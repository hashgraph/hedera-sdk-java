# Create an account

The account represents your account specific to the Hedera network. Accounts are required to utilize the Hedera network services and to pay network transaction fees. Hedera account IDs have the format: x.y.z. eg 0.0.3. where:

x represents the shard number \(shardId\). It will default to 0 today, as Hedera only performs in one shard.  
y represents the realm number \(realmId\). It will default to 0 today, as realms are not yet supported.  
z represents the account number.  
Together these values make up your accountId. When an accountId is requested, be sure all three values are included.

## Basic

The easiest way to create an account is using `.createAccount()`. with the simple client. `createAccount()` requires two properties, the public key to be associated with the new account and the initial balance in tinybars.

```text
client.setMaxTransactionFee().createAccount(PublicKey, initialBalance);
```

## Advanced

Additional properties can be set when creating a new account. The properties and their descriptions can be found below.

* `setKey()` : the public key to be associated with the new account
* `setInitialBalance()` : the initial balance for the account in tinybars
* `setTransactionFee()` : the transaction fee for the account create transaction
* `setAutoRenewPeriod()` : the period of time in which the account should renew 
* `setReceiverSignatureRequired` : transaction requires the signature of the receiver

```text
new AccountCreateTransaction()
  .setKey()
  .setInitialBalance()
  .setTransactionfee()
  .setAutoRenewPeriod()
  .setReceiverSignatureRequired()
  .setReceiveRecordThreshold()
  .setSendRecordThreshold()
```

