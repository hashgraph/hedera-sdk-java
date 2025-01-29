// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk.java;

import io.grpc.MethodDescriptor;
import java.util.Objects;
import javax.annotation.Nullable;
import org.hiero.sdk.java.proto.ConsensusGetTopicInfoQuery;
import org.hiero.sdk.java.proto.ConsensusServiceGrpc;
import org.hiero.sdk.java.proto.QueryHeader;
import org.hiero.sdk.java.proto.Response;
import org.hiero.sdk.java.proto.ResponseHeader;

/**
 * Retrieve the latest state of a topic.
 * <p>
 * This method is unrestricted and allowed on any topic by any payer account.
 */
public final class TopicInfoQuery extends Query<TopicInfo, TopicInfoQuery> {
    @Nullable
    TopicId topicId = null;

    /**
     * Constructor.
     */
    public TopicInfoQuery() {}

    /**
     * Extract the topic id.
     *
     * @return                          the topic id
     */
    @Nullable
    public TopicId getTopicId() {
        return topicId;
    }

    /**
     * Set the topic to retrieve info about (the parameters and running state of).
     *
     * @param topicId The TopicId to be set
     * @return {@code this}
     */
    public TopicInfoQuery setTopicId(TopicId topicId) {
        Objects.requireNonNull(topicId);
        this.topicId = topicId;
        return this;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (topicId != null) {
            topicId.validateChecksum(client);
        }
    }

    @Override
    void onMakeRequest(org.hiero.sdk.java.proto.Query.Builder queryBuilder, QueryHeader header) {
        var builder = ConsensusGetTopicInfoQuery.newBuilder();
        if (topicId != null) {
            builder.setTopicID(topicId.toProtobuf());
        }

        queryBuilder.setConsensusGetTopicInfo(builder.setHeader(header));
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getConsensusGetTopicInfo().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(org.hiero.sdk.java.proto.Query request) {
        return request.getConsensusGetTopicInfo().getHeader();
    }

    @Override
    TopicInfo mapResponse(Response response, AccountId nodeId, org.hiero.sdk.java.proto.Query request) {
        return TopicInfo.fromProtobuf(response.getConsensusGetTopicInfo());
    }

    @Override
    MethodDescriptor<org.hiero.sdk.java.proto.Query, Response> getMethodDescriptor() {
        return ConsensusServiceGrpc.getGetTopicInfoMethod();
    }
}
