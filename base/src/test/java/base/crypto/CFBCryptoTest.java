package base.crypto;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CFBCryptoTest {
    CFBCrypto cfb = new CFBCrypto(256, 16);
    String plain = "Mikasa";
    byte[] iv = cfb.generateIv();
    byte[] k = cfb.generateKey();
    private byte[] enc = null;


    @Test
    public void encrypt() {
        enc = cfb.encrypt(plain.getBytes(), iv, k);
    }

    @After
    public void decrypt() {
        System.out.println(new String(cfb.decrypt(enc, iv, k)));
    }
}