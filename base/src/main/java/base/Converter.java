package base;

/**
 * @author kikyou
 * @date 2020/2/1
 */
public class Converter {

    /**
     * 因为-1的b一字节二进制表示为1111 1111 所以我们可以这样做 但注意<strong>这样虽然实现简单 但是如果直接写入到ByteBuf中的话是小端字节序</strong>
     *
     * @param val 待被转化的值
     * @return byte数组索引 从 0 到 7 索引值越高 对应的位数越高
     */
    public static byte[] convertLong2ByteLittleEnding(long val) {
        byte[] ans = new byte[8];
        byte helper = -1;
        for (int i = 7; i >= 0; i--) {
            ans[i] = (byte) (val & helper);
            val = val >>> 8;
        }
        return ans;
    }

    public static byte[] convertLong2ByteBigEnding(long val) {
        byte[] ans = new byte[8];
        byte helper = -1;
        int count = 56;
        for (int i = 7; i >= 0; i--) {
            ans[i] = (byte) ((val >>> count) & helper);
            count -= 8;
        }
        return ans;
    }

    public static byte[] convertInteger2ByteBigEnding(int val) {
        byte[] ans = new byte[4];
        byte helper = -1;
        for (int i = 3; i >= 0; i--) {
            ans[i] = (byte) (val & helper);
            val = val >>> 8;
        }
        return ans;
    }

    public static byte[] convertInteger2ByteLittleEnding(int val) {
        byte[] ans = new byte[4];
        int count = 24;
        byte helper = -1;
        for (int i = 3; i >= 0; i--) {
            ans[i] = (byte) ((val >>> count) & helper);
            count -= 8;
        }
        return ans;
    }

    public static byte[] convertShort2ByteArray(short val) {
        byte[] ans = new byte[2];
        int count = 8;
        ans[0] = (byte)(val >>> 4);
        ans[1] = (byte)(val & 0xf);
        return ans;
    }


    public static void main(String[] args) throws Exception {
        for (byte b : convertInteger2ByteBigEnding(255)){
            System.out.printf("%d\t", b);
        }
    }
}