// SPDX-License-Identifier: Apache-2.0
package com.hiero.tck.methods.sdk.response;

import com.hiero.sdk.Status;
import lombok.Data;

@Data
public class TokenResponse {
    private final String tokenId;
    private final Status status;
}
