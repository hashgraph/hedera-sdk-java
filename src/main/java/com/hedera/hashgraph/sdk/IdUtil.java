package com.hedera.hashgraph.sdk;

import com.google.common.base.Splitter;

public final class IdUtil {

    private IdUtil() {  }

    public static long[] parseIdString(String id) {
        var rawNums = Splitter.on('.')
            .split(id)
            .iterator();

        return new long[] {
            Long.parseLong(rawNums.next()),
            Long.parseLong(rawNums.next()),
            Long.parseLong(rawNums.next())
        };
    }
}
