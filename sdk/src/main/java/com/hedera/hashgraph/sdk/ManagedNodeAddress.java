package com.hedera.hashgraph.sdk;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.regex.Pattern;

class ManagedNodeAddress {
    private static final Pattern HOST_AND_PORT = Pattern.compile("^(?<address>\\S+):(?<port>\\d+)$");
    private static final Pattern IN_PROCESS = Pattern.compile("^in-process:(?<name>\\S+)$");

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
            var address = hostAndPortMatcher.group("address");
            var port = hostAndPortMatcher.group("port");

            return new ManagedNodeAddress(null, address, Integer.parseInt(port));
        } else if (inProcessMatcher.find()) {
            return new ManagedNodeAddress(inProcessMatcher.group("name"), null, 0);
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
        return port == 50212 || port == 433;
    }

    public ManagedNodeAddress toInsecure() {
        var port = this.port;

        switch (this.port) {
            case 50212:
                port = 50211;
                break;
            case 433:
                port = 5600;
        }

        return new ManagedNodeAddress(name, address, port);
    }

    public ManagedNodeAddress toSecure() {
        var port = this.port;

        switch (this.port) {
            case 50211:
                port = 50212;
                break;
            case 5600:
                port = 433;
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
        return Objects.equals(getName(), that.getName()) && Objects.equals(getAddress(), that.getAddress()) && Objects.equals(getPort(), that.getPort());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getAddress(), getPort());
    }
}
