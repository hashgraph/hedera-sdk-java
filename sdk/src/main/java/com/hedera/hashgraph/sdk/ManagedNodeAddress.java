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

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Internal utility class.
 */
class ManagedNodeAddress {
    private static final Pattern HOST_AND_PORT = Pattern.compile("^(\\S+):(\\d+)$");
    private static final Pattern IN_PROCESS = Pattern.compile("^in-process:(\\S+)$");
    static final int PORT_MIRROR_PLAIN = 5600;
    static final int PORT_MIRROR_TLS = 443;
    static final int PORT_NODE_PLAIN = 50211;
    static final int PORT_NODE_TLS = 50212;

    // If address is `in-process:.*` this will contain the right side of the `:`
    @Nullable
    private final String name;

    @Nullable
    private final String address;
    private final int port;

    /**
     * Constructor.
     *
     * @param name                      the name part
     * @param address                   the address part
     * @param port                      the port part
     */
    public ManagedNodeAddress(@Nullable String name, @Nullable String address, int port) {
        this.name = name;
        this.address = address;
        this.port = port;
    }

    /**
     * Create a managed node address fom a string.
     *
     * @param string                    the string representation
     * @return                          the new managed node address
     */
    public static ManagedNodeAddress fromString(String string) {
        var hostAndPortMatcher = HOST_AND_PORT.matcher(string);
        var inProcessMatcher = IN_PROCESS.matcher(string);

        if (hostAndPortMatcher.matches() && hostAndPortMatcher.groupCount() == 2) {
            var address = hostAndPortMatcher.group(1);
            var port = hostAndPortMatcher.group(2);

            return new ManagedNodeAddress(null, address, Integer.parseInt(port));
        } else if (inProcessMatcher.matches() && inProcessMatcher.groupCount() == 1) {
            return new ManagedNodeAddress(inProcessMatcher.group(1), null, 0);
        } else {
            throw new IllegalStateException("failed to parse node address");
        }
    }

    /**
     * Extract the name.
     *
     * @return                          the name
     */
    public String getName() {
        return name;
    }

    /**
     * Extract the address.
     *
     * @return                          the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Extract the port.
     *
     * @return                          the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Are we in process?
     *
     * @return                          are we in process
     */
    public boolean isInProcess() {
        return name != null;
    }

    /**
     * Are we secure?
     *
     * @return                          are we secure
     */
    public boolean isTransportSecurity() {
        return port == PORT_NODE_TLS || port == PORT_MIRROR_TLS;
    }

    /**
     * Create a new insecure managed node.
     *
     * @return                          the insecure managed node address
     */
    public ManagedNodeAddress toInsecure() {
        var port = switch (this.port) {
            case PORT_NODE_TLS -> PORT_NODE_PLAIN;
            case PORT_MIRROR_TLS -> PORT_MIRROR_PLAIN;
            default -> this.port;
        };

        return new ManagedNodeAddress(name, address, port);
    }

    /**
     * Create a new managed node.
     *
     * @return                          the secure managed node address
     */
    public ManagedNodeAddress toSecure() {
        var port = switch (this.port) {
            case PORT_NODE_PLAIN -> PORT_NODE_TLS;
            case PORT_MIRROR_PLAIN -> PORT_MIRROR_TLS;
            default -> this.port;
        };

        return new ManagedNodeAddress(name, address, port);
    }

    @Override
    public String toString() {
        if (name != null) {
            return name;
        }

        return address + ":" + port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManagedNodeAddress that = (ManagedNodeAddress) o;
        return Objects.equals(getName(), that.getName()) && Objects.equals(getAddress(), that.getAddress()) && port == that.port;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getAddress(), getPort());
    }
}
