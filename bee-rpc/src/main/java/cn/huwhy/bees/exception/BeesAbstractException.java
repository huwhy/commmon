package cn.huwhy.bees.exception;

import cn.huwhy.bees.rpc.RpcContext;

public abstract class BeesAbstractException extends RuntimeException {
    private static final long serialVersionUID = -8742311167276890503L;

    protected BeesErrorMsg beesErrorMsg = BeesErrorMsgConstant.FRAMEWORK_DEFAULT_ERROR;
    protected String       errorMsg     = null;

    public BeesAbstractException() {
        super();
    }

    public BeesAbstractException(BeesErrorMsg beesErrorMsg) {
        super();
        this.beesErrorMsg = beesErrorMsg;
    }

    public BeesAbstractException(String message) {
        super(message);
        this.errorMsg = message;
    }

    public BeesAbstractException(String message, BeesErrorMsg beesErrorMsg) {
        super(message);
        this.beesErrorMsg = beesErrorMsg;
        this.errorMsg = message;
    }

    public BeesAbstractException(String message, Throwable cause) {
        super(message, cause);
        this.errorMsg = message;
    }

    public BeesAbstractException(String message, Throwable cause, BeesErrorMsg beesErrorMsg) {
        super(message, cause);
        this.beesErrorMsg = beesErrorMsg;
        this.errorMsg = message;
    }

    public BeesAbstractException(Throwable cause) {
        super(cause);
    }

    public BeesAbstractException(Throwable cause, BeesErrorMsg beesErrorMsg) {
        super(cause);
        this.beesErrorMsg = beesErrorMsg;
    }

    @Override
    public String getMessage() {
        if (beesErrorMsg == null) {
            return super.getMessage();
        }

        String message;

        if (errorMsg != null && !"".equals(errorMsg)) {
            message = errorMsg;
        } else {
            message = beesErrorMsg.getMessage();
        }

        return "error_message: " + message + ", status: " + beesErrorMsg.getStatus() + ", error_code: " + beesErrorMsg.getErrorCode()
                + ",r=" + RpcContext.getContext().getRequestId();
    }

    public int getStatus() {
        return beesErrorMsg != null ? beesErrorMsg.getStatus() : 0;
    }

    public int getErrorCode() {
        return beesErrorMsg != null ? beesErrorMsg.getErrorCode() : 0;
    }

    public BeesErrorMsg getBeesErrorMsg() {
        return beesErrorMsg;
    }
}
