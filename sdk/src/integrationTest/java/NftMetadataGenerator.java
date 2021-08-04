import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class NftMetadataGenerator {
    public static List<byte[]> generate(byte metadataCount) {
        List<byte[]> metadatas = new ArrayList<>();
        for(byte i = 0; i < metadataCount; i++) {
            byte[] md = {i};
            metadatas.add(md);
        }
        return metadatas;
    }

    public static List<byte[]> generateOneLarge() {
        return Collections.singletonList(new byte[101]);
    }
}
