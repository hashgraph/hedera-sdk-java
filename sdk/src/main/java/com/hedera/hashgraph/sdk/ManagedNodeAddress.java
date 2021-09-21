package com.hedera.hashgraph.sdk;

import java.util.Objects;

class ManagedNodeAddress {
    private static final String IN_PROCESS = "in-process:";

    // If address is `in-process:.*` this will contain the right side of the `:`
    private String name;
    private String address;
    private Integer port;
    private boolean transportSecurity;

    ManagedNodeAddress() {
    }

    static ManagedNodeAddress fromString(String string) {
        var nodeAddress = new ManagedNodeAddress();

        if (string.matches("^.*:\\d+$")) {
            var index = string.lastIndexOf(':');
            var address = string.substring(0, index);
            var port = string.substring(index + 1);

            nodeAddress.setAddress(address)
                .setPort(Integer.parseInt(port));
        } else if (string.startsWith(IN_PROCESS)) {
            nodeAddress.setName(string.substring(IN_PROCESS.length()));
        } else {
            nodeAddress.setAddress(string);
        }

        return nodeAddress;
    }

    public String getName() {
        return name;
    }

    public ManagedNodeAddress setName(String name) {
        this.name = name;
        return this;
    }

    String getAddress() {
        return address;
    }

    ManagedNodeAddress setAddress(String address) {
        this.address = address;
        return this;
    }

    int getPort() {
        return port;
    }

    ManagedNodeAddress setPort(int port) {
        this.port = port;

        switch (port) {
            case 5600:
            case 50211:
                transportSecurity = false;
                break;
            case 433:
            case 50212:
                transportSecurity = true;
                break;
        }

        return this;
    }

    boolean getTransportSecurity() {
        return transportSecurity;
    }

    ManagedNodeAddress setTransportSecurity(boolean transportSecurity) {
        this.transportSecurity = transportSecurity;
        return this;
    }

    boolean isInProcess() {
        return name != null;
    }

    @Override
    public String toString() {
        if (name != null) {
            return name;
        }

        if (port == null) {
            return address;
        }

        var s = address;

        switch (port) {
            case 5600:
            case 433:
                s += ":" + (transportSecurity ? 433 : 5600);
                break;
            case 50211:
            case 50212:
                s += ":" + (transportSecurity ? 50212 : 50211);
            default:
                // Do nothing
        }

        return s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManagedNodeAddress that = (ManagedNodeAddress) o;
        return getTransportSecurity() == that.getTransportSecurity() && Objects.equals(getName(), that.getName()) && Objects.equals(getAddress(), that.getAddress()) && Objects.equals(getPort(), that.getPort());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getAddress(), getPort(), getTransportSecurity());
    }
}
