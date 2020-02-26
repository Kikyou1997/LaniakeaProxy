package base;

import io.netty.buffer.ByteBuf;

/**
 * below code is from littleproxy--kikyou
 * @author Brian Wellington
 */
public class HexDump {
    private static final char [] hex = "0123456789ABCDEF".toCharArray();

    /**
     * Dumps a byte array into hex format.
     * @param description If not null, a description of the data.
     * @param b The data to be printed.
     * @param offset The start of the data in the array.
     * @param length The length of the data in the array.
     */
    public static String dump(String description, byte [] b, int offset, int length) {
        StringBuffer sb = new StringBuffer();

        sb.append(length + "b");
        if (description != null)
            sb.append(" (" + description + ")");
        sb.append(':');

        int prefixlen = sb.toString().length();
        prefixlen = (prefixlen + 8) & ~ 7;
        sb.append('\t');

        int perline = (80 - prefixlen) / 3;
        for (int i = 0; i < length; i++) {
            if (i != 0 && i % perline == 0) {
                sb.append('\n');
                for (int j = 0; j < prefixlen / 8 ; j++)
                    sb.append('\t');
            }
            int value = (int)(b[i + offset]) & 0xFF;
            sb.append(hex[(value >> 4)]);// store the value of four digits high
            sb.append(hex[(value & 0xF)]);// store the value of four digits low
            sb.append(' ');
        }
        sb.append('\n');
        return sb.toString();
    }

    public static String dump(String s, byte [] b) {
        return dump(s, b, 0, b.length);
    }

    public static String dump(ByteBuf buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        buf.readerIndex(0);
        return dump(null, bytes);
    }

}
