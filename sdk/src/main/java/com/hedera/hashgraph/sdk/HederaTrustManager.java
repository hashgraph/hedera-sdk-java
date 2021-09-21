package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.net.ssl.X509TrustManager;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

class HederaTrustManager implements X509TrustManager {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Nullable
    private final String certHash;

    HederaTrustManager(@Nullable ByteString certHash) {
        if (certHash == null || certHash.isEmpty()) {
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
            logger.warn("skipping certificate check since no cert hash was found");
            return;
        }

        for (var cert : chain) {
            var certHash = new byte[0];

            try {
                certHash = MessageDigest.getInstance("SHA-384").digest(cert.getEncoded());
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("Failed to find SHA-384 digest for certificate hashing", e);
            }

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
