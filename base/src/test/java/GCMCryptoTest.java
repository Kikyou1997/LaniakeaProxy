
import base.arch.HexDump;
import base.crypto.GCMCrypto;
import org.junit.After;
import org.junit.Test;


public class GCMCryptoTest {
    GCMCrypto gcm = new GCMCrypto(256, 16, 16);
    String plain = "Convid-19";
    byte[] iv = gcm.generateIv();
    byte[] k = gcm.generateKey();
    private byte[] enc = null;

    @Test
    public void encrypt() {
        enc = gcm.encrypt(plain.getBytes(), iv, k);
        System.out.println(iv.length == 16);
        System.out.println(HexDump.dump("", enc));
    }

    @After
    public void decrypt() {
        System.out.println(new String(gcm.decrypt(enc, iv, k)));
    }
}
