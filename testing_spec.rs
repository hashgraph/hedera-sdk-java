enum InputValue {
    String(String),
    Array(Vec<String>),
}

struct Input {
    key: String,
    value: InputValue,
}

enum NetworkValue {
    String(String),
    Object(HashMap<String, String>),
}

enum NetworkValue {
    String(String),
    Array(Vec<String>),
}

struct OpeatorConfig {
    account_id: String,
    private_key: String,
}

struct ClientConfig {
    operator: OperatorConfig,
    network: NetworkValue,
    mirror_network: MirrorNetworkValue,
}

#[serde(tag = "type")]
enum SdkRequest {
    AccountCreateTransaction(AccountCreateTransaction),
    TopicCreateTransaction(TopicCreateTransaction),
    ScheduleCreateTransaction(ScheduleCreateTransaction),
    ScheduleInfoQuery(ScheduleInfoQuery),
    TransactionReceiptQuery(TransactionReceiptQuery),
}

#[serde(untagged)]
enum SdkResponse {
    AccountCreateTransaction(TransactionResponse),
    TopicCreateTransaction(TransactionResponse),
    ScheduleCreateTransaction(TransactionResponse),
    ScheduleInfoQuery(ScheduleInfo),
    TransactionReceiptQuery(TransactionReceipt),
}

struct TestRequest {
    /// The actual SDK request
    request: SdkRequest,

    /// List of keys to sign this transaction with.
    sign: Vec<String>,
}

struct Precheck {
    /// The expected precheck code
    #[serde(default = "OK")]
    status: String
}

struct TransactionReceipt {
    /// The expected receipt status
    /// If the expected resposne for the request is not `TransactionReceipt` this will be ignored
    #[serde(default = "SUCCESS")]
    status: String
}

struct TestResponse {
    precheck: Option<Precheck>,
    receipt: Option<TransactionReceipt>,

    /// The response data for the request
    /// If the request type was `*Transaction` the response will be:
    ///  - `TransactionResponse`: if `receipt` is `false`
    ///  - `TransactionReceipt`: if `receipt` is `true`
    /// If the request type was `*Query` the response will be the particular
    /// query's response type: e.g. `ScheduleInfoQuery` will expect the response to be
    /// of type `ScheduleInfo`
    data: Option<SdkResponse>,
}

struct TestUnit {
    // The name of the this test unit
    name: Option<String>,

    // The request to execute
    request: TestRequest,

    // The response expected from the request execution
    response: Option<TestResponse>,

    /// Should this request be deleted after all the testing request have run
    /// This is only applicable to `*CreateTransaction` requests and will be ignored for all other
    /// requests.
    #[serde(default = true)]
    delete: boolean,

    /// Should the testing framework automatically fetch the transaction receipt
    /// for this request.
    /// If this request cannot have a receipt fetched, this will be ignored
    #[serde(default = true)]
    receipt: boolean,
}

/// Rules:
/// Any value that begins with `$this` will invoke the testing framework to do something
///
/// Proeprties of `$this`
///
/// - `input`: retreives the value for the key specified after
///    e.g. `$this.input.key1` will retreive the value in string form for `key1` inside `input` of
///    configuration file
///
/// - `client`: retreives some property from the client configuraiton
///   currently only supports two properties
///    - `operatorKey`: get the operator key
///    - `operatorId`: get the operator id
///   e.g. `$this.client.operatorKey` will get the clients operator key
///
/// - `test`: retreives data for some previously defined test request with a given name
///   e.g. if we create a request
///   ```
///   {
///       "name": "accountCreateTransaction",
///       "request": {
///           "type": "AccountCreateTransaction",
///           "data": {
///               "key": "$this.input.key1"
///           }
///       }
///   }
///   ```
///   We can then use `$this.test.accountCreateTransaction" to access the *response* of this
///   request. This means if `receipt` is set to true the response will be `TransactionReceipt`
///   otherwise it will be `TransactionResponse`
///
/// - `assert`: assert some conditional
///   - `nonNull`: assert the value is not null
///   - `null`: assert the value is null
///
/// - `generate`: geneate some value
///   - `key`: generate a private key `PrivateKey.generate()`
///   - `timestamp`: generate a timestamp
///
struct TestConfig {
    input: Vec<Input>,
    client: ClientConfig,
    test: Vec<TestUnit>,
}
