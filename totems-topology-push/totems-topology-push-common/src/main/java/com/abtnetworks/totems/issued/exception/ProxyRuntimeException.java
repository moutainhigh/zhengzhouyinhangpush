package com.abtnetworks.totems.issued.exception;



/**
 * @author luwei
 * @date 2019/9/5
 */
public class ProxyRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;


    public ProxyRuntimeException(String message) {
        super(message);
    }

    public ProxyRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProxyRuntimeException(Throwable cause) {
        super(cause);
    }

    protected ProxyRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);

    }


}
