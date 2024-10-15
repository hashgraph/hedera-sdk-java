/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2024 Hedera Hashgraph, LLC
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

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.ServiceEndpoint;

import java.util.Arrays;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Utility class used internally by the sdk.
 */
public class Endpoint implements Cloneable {

    @Nullable
    byte[] address = null;

    int port;

    String domainName = "";

    /**
     * Constructor.
     */
    public Endpoint() {
    }

    /**
     * Create an endpoint object from a service endpoint protobuf.
     *
     * @param serviceEndpoint           the service endpoint protobuf
     * @return                          the endpoint object
     */
    static Endpoint fromProtobuf(ServiceEndpoint serviceEndpoint) {
        var port = (int) (serviceEndpoint.getPort() & 0x00000000ffffffffL);

        if (port == 0 || port == 50111) {
            port = 50211;
        }

        return new Endpoint()
            .setAddress(serviceEndpoint.getIpAddressV4().toByteArray())
            .setPort(port)
            .setDomainName(serviceEndpoint.getDomainName());
    }

    /**
     * Extract the ipv4 address.
     *
     * @return                          the ipv4 address
     */
    @Nullable
    public byte[] getAddress() {
        return address;
    }

    /**
     * Assign the ipv4 address.
     *
     * @param address                   the desired ipv4 address
     * @return {@code this}
     */
    public Endpoint setAddress(byte[] address) {
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
     * Extract the domain name.
     *
     * @return                          the domain name
     */
    public String getDomainName() {
        return domainName;
    }

    /**
     * Assign the desired domain name.
     *
     * @param domainName                      the desired domain name
     * @return {@code this}
     */
    public Endpoint setDomainName(String domainName) {
        this.domainName = domainName;
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
            builder.setIpAddressV4(ByteString.copyFrom(address));
        }

        builder.setDomainName(domainName);

        return builder.setPort(port).build();
    }

    @Override
    public String toString() {
        if (this.domainName != null && !this.domainName.isEmpty()) {
            return domainName + ":" + port;
        } else {
            return ((int) address[0] & 0x000000FF) + "." + ((int) address[1] & 0x000000FF) + "." +
            ((int) address[2] & 0x000000FF) + "." + ((int) address[3] & 0x000000FF) +
                ":" + port;
        }
    }

    @Override
    public Endpoint clone() {
        try {
            Endpoint clone = (Endpoint) super.clone();
            clone.address = address != null ? address.clone() : null;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
