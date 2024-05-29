package com.hedera.hashgraph.tck.methods.sdk.response;

import lombok.Data;

@Data
public class AccountCreateResponse {
    private String accountId = "";
    private String status = "";
}
