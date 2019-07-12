package com.hedera.hashgraph.sdk;

import com.google.common.base.Splitter;

public final class IdUtil {

    private IdUtil() {
    }

    @FunctionalInterface
    public interface WithIdNums<R> {
        R apply(long shardNum, long realmNum, long entityNum);
    }

    public static <R> R parseIdString(String id, WithIdNums<R> withIdNums) {
        var rawNums = Splitter.on('.')
            .split(id)
            .iterator();
        R newId;
        try {
            newId = withIdNums.apply(Long.parseLong(rawNums.next()), Long.parseLong(rawNums.next()), Long.parseLong(rawNums.next()));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Id format, should be in format {shardNum}.{realmNum}.{idNum}");
        }
        return newId;
    }
}
