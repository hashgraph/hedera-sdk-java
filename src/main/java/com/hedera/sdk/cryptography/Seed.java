package com.hedera.sdk.cryptography;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;


public class Seed {
	private byte[] data;

	public Seed(byte[] data) {
		if (data == null) {
			throw new InvalidParameterException("data should not be null");
		}
		byte crc = crc8(Arrays.copyOf(data, data.length - 1));

		if (data[data.length - 1] != crc) {
			throw new InvalidParameterException(
					"Invalid data: fails the cyclic redundency check");
		}
		this.data = data;
	}

	public static Seed fromEntropy(byte[] entropy) {
		if (entropy == null) {
			throw new InvalidParameterException("data should not be null");
		}
		byte crc = crc8(entropy);
		byte[] data = new byte[entropy.length + 1];
		data[data.length - 1] = crc;
		for (int i = 0; i < entropy.length; i++) {
			data[i] = entropy[i];
		}
		return new Seed(data);
	}

	public static Seed fromWordList(String[] allWords) {
		return fromWordList(Arrays.asList(allWords));

	}

	public static Seed fromWordList(List<String> allWords) {
		String recoveryWords = "";
		for (int i = 0; i < allWords.size(); i++) {
			recoveryWords += allWords.get(i);
			recoveryWords += " ";
		}
		Reference reference = new Reference(recoveryWords);
		return Seed.fromEntropy(reference.toBytes());

	}

	public List<String> toWords() {
		byte[] shortdata = new byte[this.data.length - 1];
		for (int i = 0; i < shortdata.length; i++) {
			shortdata[i] = this.data[i];
		}
		Reference reference = new Reference(shortdata);
		return reference.toWordsList();
	}

	/**
	 * calculate the checksum of all but the last byte in data. according to
	 * http://www.ece.cmu.edu/~koopman/roses/dsn04/koopman04_crc_poly_embedded.pdf Koopman says 0xA6 (1
	 * 0100110) is a good polynomial choice, which is x^8 + x^6 + x^3 + x^2 + x^1 . The following code uses
	 * 0xB2 (1 0110010), which is 0xA6 with the bits reversed (after the first bit), which is needed for
	 * this code.
	 *
	 * @param data
	 * 		the data to find checksum for (where the last byte does not affect the checksum)
	 * @return the checksum
	 */
	public static byte crc8(byte[] data) {
		int crc = 0xFF;
		for (int i = 0; i < data.length - 1; i++) {
			crc ^= byteToUnsignedInt(data[i]);
			for (int j = 0; j < 8; j++) {
				crc = (crc >>> 1) ^ (((crc & 1) == 0) ? 0 : 0xB2);
			}
		}
		return (byte) (crc ^ 0xFF);
	}

	public byte[] toBytes() {
		byte[] result = Arrays.copyOfRange(data, 0, data.length - 1);
		return result;
	}

	public static int byteToUnsignedInt(byte b) {
		return 0x00 << 24 | b & 0xff;
	}

}
