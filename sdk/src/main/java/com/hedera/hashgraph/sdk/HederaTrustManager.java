package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
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

    HederaTrustManager(@Nullable ByteString certHash) {
        if (certHash == null) {
            this.certHash = null;
        } else {
            this.certHash = new String(certHash.toByteArray(), StandardCharsets.UTF_8);
        }
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
            var certEncoded = cert.getEncoded();

            digest.update(certEncoded, 0, certEncoded.length);
            digest.doFinal(certHash, 0);

            if (this.certHash.equals(Hex.toHexString(certHash))) {
                return;
            }
        }

        throw new CertificateException("Failed to confirm the server's certificate from a known address book");
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
