package com.hedera.hashgraph.tck.methods.sdk.response;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Status;
import lombok.Data;

@Data
public class AccountCreateResponse {
    // TODO: revisit if we should use only `String` type in Response objects
    private final AccountId accountId;
    private final Status status;
}
