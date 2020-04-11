package base.crypto;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;

import static org.junit.Assert.*;

public class CryptoUtilTest {

    private String plainText = "Hello world";
    private byte[] enc = null;
    private byte[] iv = null;
    private byte[] sk = null;

    @Before
    public void setCrypto() {
        CryptoUtil.setCrypto(new GCMCrypto(256, 16, 16));
        iv = CryptoUtil.generateIv();
        sk = CryptoUtil.generateKey();
    }

    @Test
    public void encrypt() {
        enc = CryptoUtil.encrypt(plainText.getBytes(), sk, iv);
    }

    @After
    public void decrypt() {
        System.out.println(new String(CryptoUtil.decrypt(enc, sk, iv)));
    }


}