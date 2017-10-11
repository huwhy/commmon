package cn.huwhy.bees.exception;

/**
 * @author huwhy
 * @data 2016/11/30
 * @Desc
 */
public class AbstractException extends RuntimeException {

    private int code;

    public AbstractException() {
    }

    public AbstractException(int code) {
        this.code = code;
    }

    public AbstractException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
