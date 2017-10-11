package cn.huwhy.bees.exception;

import java.io.Serializable;

public class BeesErrorMsg implements Serializable {
    private static final long serialVersionUID = 4909459500370103048L;

    private int status;
    private int errorcode;
    private String message;

    public BeesErrorMsg(int status, int errorcode, String message) {
        this.status = status;
        this.errorcode = errorcode;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public int getErrorCode() {
        return errorcode;
    }

    public String getMessage() {
        return message;
    }

}
