package base.constants;

/**
 * @author kikyou
 * Created at 2020/1/31
 */
public interface ResponseCode {

    byte CLOCK_RESP = (byte)0Xff;
    byte AUTH_RESP = (byte)0xfe;
    byte DATA_TRANS_RESP = (byte)0xfd;
    byte CONN_ESTAB = (byte)0xfc;
    byte AUTH_FAILED = (byte)0xfb;

}
