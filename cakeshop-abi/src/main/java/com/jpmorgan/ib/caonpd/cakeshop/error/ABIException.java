package com.jpmorgan.ib.caonpd.cakeshop.error;

public class ABIException extends RuntimeException {

    public ABIException() {
        super();
    }

    public ABIException(String message, Throwable cause) {
        super(message, cause);
    }

    public ABIException(String message) {
        super(message);
    }

    public ABIException(Throwable cause) {
        super(cause);
    }

}
