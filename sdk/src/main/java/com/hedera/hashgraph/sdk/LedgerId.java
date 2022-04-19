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

import com.google.protobuf.ByteString;
import org.bouncycastle.util.encoders.Hex;

import javax.annotation.Nullable;
import java.util.Arrays;

public class LedgerId {
    final private byte[] idBytes;

    public final static LedgerId MAINNET = new LedgerId(new byte[]{0});
    public final static LedgerId TESTNET = new LedgerId(new byte[]{1});
    public final static LedgerId PREVIEWNET = new LedgerId(new byte[]{2});

    LedgerId(byte[] idBytes) {
        this.idBytes = idBytes;
    }

    public static LedgerId fromString(String string) {
        switch (string) {
            case "mainnet":
                return MAINNET;
            case "testnet":
                return TESTNET;
            case "previewnet":
                return PREVIEWNET;
            default:
                return new LedgerId(Hex.decode(string));
        }
    }

    public static LedgerId fromBytes(byte[] bytes) {
        return new LedgerId(bytes);
    }

    static LedgerId fromByteString(ByteString byteString) {
        return fromBytes(byteString.toByteArray());
    }

    @Deprecated
    public static LedgerId fromNetworkName(NetworkName networkName) {
        switch (networkName) {
            case MAINNET:
                return MAINNET;
            case TESTNET:
                return TESTNET;
            case PREVIEWNET:
                return PREVIEWNET;
            default:
                throw new IllegalArgumentException("networkName must be MAINNET, TESTNET, or PREVIEWNET");
        }
    }

    public boolean isMainnet() {
        return this.equals(MAINNET);
    }

    public boolean isTestnet() {
        return this.equals(TESTNET);
    }

    public boolean isPreviewnet() {
        return this.equals(PREVIEWNET);
    }

    boolean isKnownNetwork() {
        return isMainnet() || isTestnet() || isPreviewnet();
    }

    public String toString() {
        if (isMainnet()) {
            return "mainnet";
        } else if (isTestnet()) {
            return "testnet";
        } else if (isPreviewnet()) {
            return "previewnet";
        } else {
            return Hex.toHexString(idBytes);
        }
    }

    public byte[] toBytes() {
        return idBytes;
    }

    ByteString toByteString() {
        return ByteString.copyFrom(idBytes);
    }

    @Deprecated
    public NetworkName toNetworkName() {
        if (isMainnet()) {
            return NetworkName.MAINNET;
        } else if (isTestnet()) {
            return NetworkName.TESTNET;
        } else if (isPreviewnet()) {
            return NetworkName.PREVIEWNET;
        } else {
            return NetworkName.OTHER;
        }
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof LedgerId)) {
            return false;
        }

        LedgerId otherId = (LedgerId) o;
        return Arrays.equals(idBytes, otherId.idBytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(idBytes);
    }
}
