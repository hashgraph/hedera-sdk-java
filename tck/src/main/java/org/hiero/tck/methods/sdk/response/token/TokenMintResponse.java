package org.hiero.tck.methods.sdk.response.token;

import org.hiero.sdk.Status;

import java.util.List;

public class TokenMintResponse extends TokenResponse {
    private final String newTotalSupply;
    private final List<String> serialNumbers;

    public TokenMintResponse(String tokenId, Status status, String newTotalSupply, List<String> serialNumbers) {
        super(tokenId, status);
        this.newTotalSupply = newTotalSupply;
        this.serialNumbers = serialNumbers;
    }

    public String getNewTotalSupply() {
        return newTotalSupply;
    }

    public List<String> getSerialNumbers() {
        return serialNumbers;
    }
}
