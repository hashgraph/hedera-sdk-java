package com.hedera.hashgraph.sdk;

import javax.annotation.Nonnegative;

class ParseAddressResult {
    int status;
    Long num1;
    Long num2;
    Long num3;
    String correctChecksum;
    String givenChecksum;
    String noChecksumChecksum;
    String withChecksumFormat;

    ParseAddressResult(
        int status,
        Long num1,
        Long num2, Long num3,
        String correctChecksum,
        String givenChecksum,
        String noChecksumChecksum,
        String withChecksumFormat
    ) {
        this.status = status;
        this.num1 = num1;
        this.num2 = num2;
        this.num3 = num3;
        this.correctChecksum = correctChecksum;
        this.givenChecksum = givenChecksum;
        this.noChecksumChecksum = noChecksumChecksum;
        this.withChecksumFormat = withChecksumFormat;
    }

    ParseAddressResult(int status){
        this.status = status;
    }
}
