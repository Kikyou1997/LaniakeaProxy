import base.arch.CryptoUtil;

public class CryptoUtilTest {

    public static void main(String[] args) throws Exception{
        byte[] bytes = new byte[25];
        System.out.println(CryptoUtil.generateKey().length);
        System.out.println(CryptoUtil.encrypt(bytes, CryptoUtil.generateKey(), CryptoUtil.generateIv()).length);
    }
}
