package interfaces;

/**
 * @author kikyou
 * @date 2020/1/29
 */
public interface Auth extends Handler<Void>{

    int HASH_LENGTH = 32;

    boolean isValid(Object msg) throws Exception;

}
