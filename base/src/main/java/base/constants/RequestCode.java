package base.constants;

/**
 * @author kikyou
 * Created at 2020/1/31
 */
public interface RequestCode {
    byte GET_CLOCK_REQ = 0x1;
    byte AUTH_REQ= 0x2;
    byte DATA_TRANS_REQ = 0x4;
    byte CONNECT = 0x5;
    byte GET_USED_TRAFFIC = 0x6;
    byte GET_USED_TRAFFIC_ALL = 0x7;
}
