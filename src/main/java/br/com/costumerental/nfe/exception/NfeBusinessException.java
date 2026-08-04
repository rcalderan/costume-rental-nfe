package br.com.costumerental.nfe.exception;

public class NfeBusinessException extends RuntimeException {

    public NfeBusinessException(String message) {
        super(message);
    }

    public NfeBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
