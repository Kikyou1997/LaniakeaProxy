/**
 * @author kikyou
 * @date 2020/2/1
 */
public class Converter {
    /**
     * 因为-1的b一字节二进制表示为1111 1111 所以我们可以这样做
     * @param val 待被转化的值
     * @return byte数组索引 从 0 到 7 索引值越高 对应的位数越高
     */
    public static byte[] convertLong2Byte(long val) {
            byte[] ans = new byte[8];
            byte helper = -1;
            for (int i = 7; i >= 0; i--) {
                ans[i] = (byte) (val & helper);
                val = val >>> 8;
            }
            return ans;
    }

    public static void main(String[] args) throws Exception {

    }
}