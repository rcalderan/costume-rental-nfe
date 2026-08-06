package br.com.costumerental.nfe.exception;

public class NfeBusinessException extends RuntimeException {

    private final String xml;

    public NfeBusinessException(String message) {
        this(message, null, null);
    }

    public NfeBusinessException(String message, Throwable cause) {
        this(message, null, cause);
    }

    public NfeBusinessException(String message, String xml) {
        this(message, xml, null);
    }

    public NfeBusinessException(String message, String xml, Throwable cause) {
        super(message, cause);
        this.xml = xml;
    }

    public String getXml() {
        return xml;
    }
}
