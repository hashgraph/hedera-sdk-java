package com.hedera.hashgraph.tck.methods.sdk.response;

import com.hedera.hashgraph.sdk.Status;
import lombok.Data;

@Data
public class AccountCreateResponse {
    private final String accountId;
    private final Status status;
}
