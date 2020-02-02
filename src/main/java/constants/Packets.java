package constants;

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
}
