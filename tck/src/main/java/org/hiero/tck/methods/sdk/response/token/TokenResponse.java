// SPDX-License-Identifier: Apache-2.0
package org.hiero.tck.methods.sdk.response.token;

import com.hedera.hashgraph.sdk.Status;

public class TokenResponse {
    private String tokenId;
    private Status status;

    public TokenResponse(String tokenId, Status status) {
        this.tokenId = tokenId;
        this.status = status;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
