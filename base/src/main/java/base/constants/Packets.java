package base.constants;

/**
 * @author kikyou
 * Created at 2020/2/2
 * UNIT:byte
 */
public interface Packets {

    int FILED_HOST_LENGTH = 2;
    int FIELD_ID_LENGTH = 4;
    int FIELD_HASH_LENGTH = 32;
    int FIELD_CODE_LENGTH = 1;
    int FIELD_LENGTH_LEN = 4;
    int FIELD_IV_LENGTH = 16;
    int FILED_PORT_LENGTH = 2;
    int HEADERS_AUTH_REQ_LEN  = FIELD_CODE_LENGTH + FIELD_HASH_LENGTH;
    int HEADERS_AUTH_RESP_LEN = FIELD_CODE_LENGTH + FIELD_ID_LENGTH + FIELD_IV_LENGTH;
    int HEADERS_DATA_REQ_LEN  = FIELD_CODE_LENGTH + FIELD_ID_LENGTH + FIELD_LENGTH_LEN;
    int HEADERS_DATA_RESP_LEN = FIELD_CODE_LENGTH + FIELD_LENGTH_LEN;
    int HEADERS_CONNECT_REQ_LEN = FIELD_CODE_LENGTH + FIELD_ID_LENGTH;
    byte IPV4 = 0;
    byte IPV6 = 1;

}
