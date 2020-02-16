package base.constants;

/**
 * @author kikyou
 * Created at 2020/2/2
 * UNIT:byte
 */
public interface Packets {

    short MAGIC = 0x1234;

    int ID_LENGTH = 4;
    int HASH_LENGTH = 32;
    int MAGIC_LENGTH = 2;
    int CODE_LENGTH = 1;
    int LENGTH_FILED_LENGTH = 4;
    int IPV_LENGTH = 1;
    int IPV4_LENGTH = 4;
    int IPV6_LENGTH = 16;
    int CLOCK_LENGTH = 8;
    int IV_LENGTH = 16;
    int HOST_LENGTH = 2;

    byte IPV4 = 0;
    byte IPV6 = 1;
}
