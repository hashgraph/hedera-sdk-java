package com.hedera.hashgraph.sdk;

import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.util.Arrays;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigInteger;

public class PrivateKeyECDSA extends PrivateKey {

    private final BigInteger keyData;

    PrivateKeyECDSA(BigInteger keyData, @Nullable PublicKey publicKey) {
        this.keyData = keyData;
        this.publicKey = publicKey;
    }

    static PrivateKeyECDSA generateInternal() {
        var generator = new ECKeyPairGenerator();
        var keygenParams = new ECKeyGenerationParameters(ECDSA_SECP256K1_DOMAIN, ThreadLocalSecureRandom.current());
        generator.init(keygenParams);
        var keypair = generator.generateKeyPair();
        var privParams = (ECPrivateKeyParameters) keypair.getPrivate();
        var pubParams = (ECPublicKeyParameters) keypair.getPublic();
        return new PrivateKeyECDSA(privParams.getD(), new PublicKeyECDSA(pubParams.getQ().getEncoded(true)));
    }

    static PrivateKeyECDSA fromPrivateKeyInfoInternal(PrivateKeyInfo privateKeyInfo) {
        try {
            var privateKey = (ASN1OctetString) privateKeyInfo.parsePrivateKey();

            return new PrivateKeyECDSA(new BigInteger(1, privateKey.getOctets()), null);
        } catch (IOException e) {
            throw new BadKeyException(e);
        }
    }

    public static PrivateKey fromBytesInternal(byte[] privateKey) {
        if (privateKey.length == 32) {
            return new PrivateKeyECDSA(new BigInteger(1, privateKey), null);
        }

        // Assume a DER-encoded private key descriptor
        return fromPrivateKeyInfoInternal(PrivateKeyInfo.getInstance(privateKey));
    }

    static byte[] legacyDeriveChildKey(byte[] entropy, long index) {
        throw new IllegalStateException("ECDSA secp256k1 keys do not currently support derivation");
    }

    @Override
    public PrivateKey legacyDerive(long index) {
        throw new IllegalStateException("ECDSA secp256k1 keys do not currently support derivation");
    }

    @Override
    public boolean isDerivable() {
        return false;
    }

    @Override
    public PrivateKey derive(int index) {
        throw new IllegalStateException("ECDSA secp256k1 keys do not currently support derivation");
    }

    @Override
    public PublicKey getPublicKey() {
        if (publicKey != null) {
            return publicKey;
        }

        var q = ECDSA_SECP256K1_DOMAIN.getG().multiply(keyData);
        var publicParams = new ECPublicKeyParameters(q, ECDSA_SECP256K1_DOMAIN);
        publicKey = new PublicKeyECDSA(publicParams.getQ().getEncoded(true));
        return publicKey;
    }

    @Override
    public byte[] sign(byte[] message) {
        var hash = Crypto.calcKeccak256(message);

        var signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
        signer.init(true, new ECPrivateKeyParameters(keyData, ECDSA_SECP256K1_DOMAIN));
        BigInteger[] bigSig = signer.generateSignature(hash);

        byte[] sigBytes = Arrays.copyOf(bigIntTo32Bytes(bigSig[0]), 64);
        System.arraycopy(bigIntTo32Bytes(bigSig[1]), 0, sigBytes, 32, 32);

        return sigBytes;
    }

    @Override
    public byte[] toBytes() {
        return toBytesDER();
    }

    private static byte[] bigIntTo32Bytes(BigInteger n) {
        byte[] bytes = n.toByteArray();
        byte[] bytes32 = new byte[32];
        System.arraycopy(
            bytes, Math.max(0, bytes.length - 32),
            bytes32, Math.max(0, 32 - bytes.length),
            Math.min(32, bytes.length)
        );
        return bytes32;
    }

    @Override
    public byte[] toBytesRaw() {
        return bigIntTo32Bytes(keyData);
    }

    @Override
    public byte[] toBytesDER() {
        try {
            return new PrivateKeyInfo(
                new AlgorithmIdentifier(ID_ECDSA_SECP256K1),
                new DEROctetString(toBytesRaw())
            ).getEncoded("DER");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isED25519() { return false; }

    @Override
    public boolean isECDSA() { return true; }
}
