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
    private final NetworkName networkName;

    private final AccountId nodeAccountId;

    HederaTrustManager(@Nullable NetworkName networkName, AccountId nodeAccountId) {
        this.networkName = networkName;
        this.nodeAccountId = nodeAccountId;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (networkName == null) {
            return;
        }

        var addressBook = Network.NODE_ADDRESS_BOOK.get(networkName);
        if (addressBook == null) {
            return;
        }

        for (var cert : chain) {
            for (var nodeAddress : addressBook.nodeAddresses) {
                if (nodeAddress.accountId.equals(nodeAccountId) && !nodeAddress.certHash.isEmpty()) {
                    var digest = new SHA384Digest();
                    var certHash = new byte[digest.getDigestSize()];
                    digest.update(cert.getEncoded(), 0, cert.getEncoded().length);
                    digest.doFinal(certHash, 0);

                    var nodeAddressBookCertHash = new String(nodeAddress.certHash.toByteArray(), StandardCharsets.UTF_8);

                    if (!nodeAddressBookCertHash.equals(Hex.toHexString(certHash))) {
                        throw new CertificateException();
                    }
                }

            }
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
