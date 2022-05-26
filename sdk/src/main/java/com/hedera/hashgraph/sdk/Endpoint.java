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

import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.proto.ServiceEndpoint;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Utility class used internally by the sdk.
 */
public class Endpoint {
    @Nullable
    IPv4Address address = null;

    int port;

    /**
     * Constructor.
     */
    Endpoint() {
    }

    /**
     * Create an endpoint object from a service endpoint protobuf.
     *
     * @param serviceEndpoint           the service endpoint protobuf
     * @return                          the endpoint object
     */
    static Endpoint fromProtobuf(ServiceEndpoint serviceEndpoint) {
        @Var var port = (int) (serviceEndpoint.getPort() & 0x00000000ffffffffL);

        if (port == 0 || port == 50111) {
            port = 50211;
        }

        return new Endpoint()
            .setAddress(IPv4Address.fromProtobuf(serviceEndpoint.getIpAddressV4()))
            .setPort(port);
    }

    /**
     * Extract the ipv4 address.
     *
     * @return                          the ipv4 address
     */
    @Nullable
    public IPv4Address getAddress() {
        return address;
    }

    /**
     * Assign the ipv4 address.
     *
     * @param address                   the desired ipv4 address
     * @return {@code this}
     */
    public Endpoint setAddress(IPv4Address address) {
        this.address = address;
        return this;
    }

    /**
     * Extract the port number.
     *
     * @return                          the port number
     */
    public int getPort() {
        return port;
    }

    /**
     * Assign the desired port number.
     *
     * @param port                      the desired port number
     * @return {@code this}
     */
    public Endpoint setPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Create the protobuf.
     *
     * @return                          the protobuf representation
     */
    ServiceEndpoint toProtobuf() {
        var builder = ServiceEndpoint.newBuilder();

        if (address != null) {
            builder.setIpAddressV4(address.toProtobuf());
        }

        return builder.setPort(port).build();
    }

    @Override
    public String toString() {
        return Objects.requireNonNull(address) +
            ":" +
            port;
    }
}
