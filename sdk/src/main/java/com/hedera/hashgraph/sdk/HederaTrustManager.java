package com.hedera.hashgraph.sdk;

import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.util.encoders.Hex;

import javax.annotation.Nullable;
import javax.net.ssl.X509TrustManager;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

class HederaTrustManager implements X509TrustManager {
    @Nullable
    private final String certHash;

    HederaTrustManager(@Nullable NetworkName networkName, AccountId nodeAccountId) {
        if (networkName == null) {
            this.certHash = null;
            return;
        }

        var nodeAddressBook = Network.nodeAddressBooks.get(networkName);
        if (nodeAddressBook == null) {
            throw new IllegalStateException("Unrecognized network name");
        }

        var nodeAddress = nodeAddressBook.get(nodeAccountId);
        if (nodeAddress == null) {
            throw new IllegalStateException("could not find node account ID within address book");
        }

        this.certHash = new String(nodeAddress.certHash.toByteArray(), StandardCharsets.UTF_8);
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (certHash == null) {
            return;
        }

        for (var cert : chain) {
            var digest = new SHA384Digest();
            var certHash = new byte[digest.getDigestSize()];
            digest.update(cert.getEncoded(), 0, cert.getEncoded().length);
            digest.doFinal(certHash, 0);

            if (this.certHash.equals(Hex.toHexString(certHash))) {
                return;
            }
        }

        throw new CertificateException();
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
