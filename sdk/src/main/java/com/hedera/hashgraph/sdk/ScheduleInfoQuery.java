/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.ScheduleGetInfoQuery;
import com.hedera.hashgraph.sdk.proto.ScheduleServiceGrpc;
import io.grpc.MethodDescriptor;
import java8.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * A query that returns information about the current state of a schedule
 * transaction on a Hedera network.
 *
 * See <a href="https://docs.hedera.com/guides/docs/sdks/schedule-transaction/get-schedule-info">Hedera Documentation</a>
 */
public class ScheduleInfoQuery extends com.hedera.hashgraph.sdk.Query<ScheduleInfo, ScheduleInfoQuery> {
    @Nullable
    private ScheduleId scheduleId = null;

    /**
     * Constructor.
     */
    public ScheduleInfoQuery() {
    }

    /**
     * Extract the schedule id.
     *
     * @return                          the schedule id
     */
    @Nullable
    public ScheduleId getScheduleId() {
        return scheduleId;
    }

    /**
     * Assign the schedule id.
     *
     * @param scheduleId                the schedule id
     * @return {@code this}
     */
    public ScheduleInfoQuery setScheduleId(ScheduleId scheduleId) {
        Objects.requireNonNull(scheduleId);
        this.scheduleId = scheduleId;
        return this;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (scheduleId != null) {
            scheduleId.validateChecksum(client);
        }
    }

    @Override
    void onMakeRequest(com.hedera.hashgraph.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        var builder = ScheduleGetInfoQuery.newBuilder();
        if (scheduleId != null) {
            builder.setScheduleID(scheduleId.toProtobuf());
        }

        queryBuilder.setScheduleGetInfo(builder.setHeader(header));
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getScheduleGetInfo().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(com.hedera.hashgraph.sdk.proto.Query request) {
        return request.getScheduleGetInfo().getHeader();
    }

    @Override
    ScheduleInfo mapResponse(Response response, AccountId nodeId, Query request) {
        return ScheduleInfo.fromProtobuf(response.getScheduleGetInfo());
    }

    @Override
    MethodDescriptor<Query, Response> getMethodDescriptor() {
        return ScheduleServiceGrpc.getGetScheduleInfoMethod();
    }

    @Override
    public CompletableFuture<Hbar> getCostAsync(Client client) {
        // deleted accounts return a COST_ANSWER of zero which triggers `INSUFFICIENT_TX_FEE`
        // if you set that as the query payment; 25 tinybar seems to be enough to get
        // `Token_DELETED` back instead.
        return super.getCostAsync(client).thenApply((cost) -> Hbar.fromTinybars(Math.max(cost.toTinybars(), 25)));
    }
}
