package cn.huwhy.bees.exception;

/**
 * @author huwhy
 * @data 2016/11/30
 * @Desc
 */
public class BeesTimeOutException extends AbstractException {

    public BeesTimeOutException() {
        super(BeesErrorCodes.ERROR_TIMEOUT, "request time out");
    }

}
