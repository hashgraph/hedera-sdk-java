// SPDX-License-Identifier: Apache-2.0
package org.hiero.tck.methods.sdk.response;

import lombok.Data;
import org.hiero.sdk.Status;

@Data
public class TokenResponse {
    private final String tokenId;
    private final Status status;
}
