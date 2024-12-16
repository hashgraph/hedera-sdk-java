// SPDX-License-Identifier: Apache-2.0
package com.hiero.tck.methods.sdk.response;

import com.hiero.sdk.Status;
import lombok.Data;

@Data
public class AccountResponse {
    private final String accountId;
    private final Status status;
}
