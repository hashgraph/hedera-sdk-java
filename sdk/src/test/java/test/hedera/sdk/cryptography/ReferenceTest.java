package test.hedera.sdk.cryptography;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.util.List;

import com.hedera.sdk.cryptography.Reference;
import com.hedera.sdk.cryptography.WordList;
import org.spongycastle.util.encoders.Hex;
import java.util.Arrays;

@TestInstance(Lifecycle.PER_CLASS)
class ReferenceTest {

  private String seedHex;

  @BeforeAll
  void setup() {
    // this belongs to ED25519 seedHex
    seedHex = "cf831ccb83f7d1d6a0261e2a6f69552dbd452d7b3a8fb4f5f960e8aafcf0d32f";
    
  }

  @Test
  void testReference() {
    // Instantiate a reference object, reference1, given a private key, for example
    byte[] seed = Hex.decode(seedHex);
    Reference reference1 = new Reference(seed);
    // Return a recovery word list from this reference object
    List<String> wordList = reference1.toWordsList();
    String recoveryWords = "";
    for (int i=0; i < wordList.size(); i++) {
      recoveryWords += wordList.get(i);
      recoveryWords += " ";
    }
    // Instantiate another reference object (reference2) from the recovery words from reference1
    Reference reference2 = new Reference(recoveryWords);
    List<String> recoveryWordList = reference2.toWordsList();
    String recoveryWords2 = "";
    for (int i=0; i < recoveryWordList.size(); i++) {
      recoveryWords2 += recoveryWordList.get(i);
      recoveryWords2 += " ";
    }
    // Prove that the returned recovery words match
    assertEquals(recoveryWords, recoveryWords2);

  }

  @Test
  void testLenInBitSize() {
    int expectedValue1 = Reference.lenInBitSize(128, 4096);
    assertEquals(expectedValue1, 12);
    int expectedValue2 = Reference.lenInBitSize(256, 4096);
    assertEquals(expectedValue2, 22);
    int expectedValue3 = Reference.lenInBitSize(384, 4096);
    assertEquals(expectedValue3, 33);
  }
  @Test
  void testWordIndices() {
    String dataString1 = "hello world you";
    List<String> allWords = Reference.wordIndices(dataString1);
    List<String> expected = Arrays.asList("hello", "world", "you");
    assertEquals(expected, allWords);

    String dataString2 = "hello world 123";
    List<String> allWords2 = Reference.wordIndices(dataString2);
    List<String> expected2 = Arrays.asList("hello", "world");
    assertEquals(expected2, allWords2);

    String dataString3 = "hello world 世界 朋友";
    List<String> allWords3 = Reference.wordIndices(dataString3);
    List<String> expected3 = Arrays.asList("hello", "world");
    assertEquals(expected3, allWords3);

    // try latin
    String dataString4 = "hello world amicis orbis";
    List<String> allWords4 = Reference.wordIndices(dataString4);
    List<String> expected4 = Arrays.asList("hello", "world", "amicis", "orbis");
    assertEquals(expected4, allWords4);

    String dataString5 = "hello world touché";
    List<String> allWords5 = Reference.wordIndices(dataString5);
    List<String> expected5 = Arrays.asList("hello", "world");
    assertNotEquals(expected5, allWords5);
  }
  
  @Test
  void testToDigitsData() {
    String dataString = "embark port duly poetry front verity quote lake toast Austin pig far pink clergy allied still injury night canine frenzy gamma heel";
    List<String> allWords = Reference.wordIndices(dataString);
    List<String> words = WordList.words;
    int[] indices = Reference.allWordsExtraction(allWords, words);
    int len128Bits = Reference.lenInBitSize(128, words.size());
    int len256Bits = Reference.lenInBitSize(256, words.size());
    int len384Bits = Reference.lenInBitSize(384, words.size());
    byte[] expected = {70, 10, -107, 66, 10, 126, 88, 95, 41, -81, -105, -93, -26, -32, -36, -92, 52, -52, -92, -14, -77, 6, 61, 124, 112, -23, 97, 35, 117, 121, 90, -90, -119};
    byte[] data = Reference.toDigitsData(allWords,indices, words, len128Bits,len256Bits,len384Bits);
    assertEquals(Arrays.toString(expected), Arrays.toString(data));
  }
  
  @Test
  void testConvertRadix() {
    // prepare test values
    byte[] seed = Hex.decode(seedHex);
    Reference reference = new Reference(seed);
    byte[] data = reference.getData();
    int[] fromDigits = new int[data.length];
		for (int i = 0; i < data.length; i++) {
        fromDigits[i] = Reference.byteToUnsignedInt(data[i]);
    }
    List<String> words = WordList.words;
    int len = 22; // use hardcoded len as our test value for now
    // invoke our convertRadix method with our prepared test values
    int[] toDigits = Reference.convertRadix(fromDigits, 256, words.size(), len);
    String actualIndices = Arrays.toString(toDigits); // this returns the indexes of the words that were used as recovery
    int[] expectedDigits = new int[] {1120, 2709, 1056, 2686, 1413, 3881, 2809, 1955, 3694, 220, 2627, 1228, 2639, 691, 99, 3452, 1806, 2401, 567, 1401, 1450, 1673};
    assertEquals(Arrays.toString(expectedDigits), actualIndices);
  }

  @Test
  void testDataScramblingUnscramblingWithCrc8() {
    // test data
    boolean isHex = seedHex.matches("^[0-9a-fA-F]+$");
    // verify that seedHex is indeed hexidecimal
    assertEquals(isHex, true);
    byte[] seed = Hex.decode(seedHex);
    
    // store the data in scrambled form
    byte crc = Reference.crc8(seed);
    byte[] dataScrambled = new byte[seed.length + 1];
    dataScrambled[seed.length] = crc; // crc goes to the end
    for (int i = 0; i < seed.length; i++) {
      dataScrambled[i] = (byte) (seed[i] ^ crc);  // checksum is XORed with all previous bytes
    }

    // retrieve the data in unscrambled form
    byte[] dataUnscrambled = new byte[dataScrambled.length - 1];
		byte crc2 = dataScrambled[dataScrambled.length - 1];
		for (int i = 0; i < dataScrambled.length - 1; i++) {
			dataUnscrambled[i] = (byte) (((byte) dataScrambled[i]) ^ crc2);
    }
    
    // verify that they match
    crc2 = Reference.crc8(dataUnscrambled);
    assertEquals(crc2, crc);
    byte[] result = Hex.encode(dataUnscrambled);
    String s = new String(result);
    assertEquals(s, seedHex);
  }

  @Test
  void testCrc8() {
    byte[] seed = Hex.decode(seedHex);
    byte result = Reference.crc8(seed);
  }

  @Test
  void testByteToUnsignedInt() {
    byte[] seed = Hex.decode(seedHex);
    assertEquals(207, Reference.byteToUnsignedInt(seed[0]));
  }

  @Test
  void testToBytes() {
    byte[] seed = Hex.decode(seedHex);
    Reference reference = new Reference(seed);
    byte[] result = reference.toBytes();
  }
}