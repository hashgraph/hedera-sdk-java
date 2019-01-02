package com.hedera.sdk.cryptography;


import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.asn1.pkcs.PBKDF2Params;
import org.spongycastle.crypto.digests.SHA512Digest;
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.spongycastle.crypto.params.KeyParameter;

public class CryptoUtils {
	final static Logger logger = LoggerFactory.getLogger(CryptoUtils.class);

    public static Seed generateRandomSeed(){
    	logger.trace("Start - generateRandomSeed");
        Seed seed = Seed.fromEntropy(getSecureRandomData(23));
    	logger.trace("End - generateRandomSeed");
        return seed;
    }

    public static byte[] getSecureRandomData(int length){
    	logger.trace("Start - getSecureRandomData length {}", length);
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
    	logger.trace("End - getSecureRandomData");
        return bytes;
    }

    public static byte[] sha384Digest(byte[] message) throws NoSuchAlgorithmException {
    	logger.trace("Start - sha384Digest message {}", message);
        MessageDigest digest = null;
        digest = MessageDigest.getInstance("SHA-384");
        byte[] hash = digest.digest(message);
    	logger.trace("End - sha384Digest message {}", message);
        return hash;
    }

    public static byte[] deriveKey(byte[] seed, long index, int length) {
    	logger.trace("Start - deriveKey seed {}, index {}, length {}", seed, index, length);
        byte[] password = new byte[seed.length + Long.BYTES];
        for (int i = 0; i < seed.length; i++) {
            password[i] = seed[i];
        }
        byte[] indexData = longToBytes(index);
        int c = 0;
        for (int i = indexData.length-1; i >=0 ; i--) {
            password[seed.length + c] = indexData[i];
            c++;
        }

        byte[] salt = new byte[]{-1};

        PBKDF2Params params = new PBKDF2Params(salt,2048, length*8);

        PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA512Digest());
        gen.init(password, params.getSalt(), params.getIterationCount().intValue());

        byte[] derivedKey = ((KeyParameter)gen.generateDerivedParameters(length*8)).getKey();

        logger.trace("End - deriveKey");

        return derivedKey;
    }

    public static byte[] longToBytes(long x) {
    	logger.trace("Start - longToBytes x {}", x);
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
    	logger.trace("End - longToBytes");
        return buffer.array();
    }
}
