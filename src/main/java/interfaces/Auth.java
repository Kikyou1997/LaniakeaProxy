package interfaces;

/**
 * @author kikyou
 * @date 2020/1/29
 */
public interface Auth extends Handler{

    boolean isValid(Object msg) throws Exception;

}
