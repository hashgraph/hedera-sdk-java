package com.hedera.hashgraph.sdk;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.regex.Pattern;

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

    public ManagedNodeAddress(@Nullable String name, @Nullable String address, int port) {
        this.name = name;
        this.address = address;
        this.port = port;
    }

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

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public boolean isInProcess() {
        return name != null;
    }

    public boolean isTransportSecurity() {
        return port == PORT_NODE_TLS || port == PORT_MIRROR_TLS;
    }

    public ManagedNodeAddress toInsecure() {
        var port = this.port;

        switch (this.port) {
            case PORT_NODE_TLS:
                port = PORT_NODE_PLAIN;
                break;
            case PORT_MIRROR_TLS:
                port = PORT_MIRROR_PLAIN;
        }

        return new ManagedNodeAddress(name, address, port);
    }

    public ManagedNodeAddress toSecure() {
        var port = this.port;

        switch (this.port) {
            case PORT_NODE_PLAIN:
                port = PORT_NODE_TLS;
                break;
            case PORT_MIRROR_PLAIN:
                port = PORT_MIRROR_TLS;
        }

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
        // ports are deliberately not compared.  The secure and non-secure version of the same address should evaluate to equal.
        // This is especially important for setNetwork() in ManagedNetwork
        return Objects.equals(getName(), that.getName()) && Objects.equals(getAddress(), that.getAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getAddress(), getPort());
    }
}
