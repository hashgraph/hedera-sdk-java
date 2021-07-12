package com.hedera.hashgraph.sdk;

class ParseAddressResult {
    int status = 0;
    Long num1 = 0L;
    Long num2 = 0L;
    Long num3 = 0L;
    String correctChecksum = new String();
    String givenChecksum = new String();
    String noChecksumChecksum = new String();
    String withChecksumFormat = new String();

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

    ParseAddressResult(){
    }
}
