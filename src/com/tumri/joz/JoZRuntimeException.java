package com.tumri.joz;

/**
 * Top level runtime exception (unchecked) that gets thrown by the JoZ Server. All the implementation specific
 * unchecked exceptions should be encapsulated into this exception class, to facilitate client to catch only single top-level
 * runtime exception within its code, if required.
 *
 * @author  bpatel
 */
public class JoZRuntimeException extends RuntimeException {

    public JoZRuntimeException() {
    }

    public JoZRuntimeException(String string) {
        super(string);
    }

    public JoZRuntimeException(String string, Throwable throwable) {
        super(string, throwable);
    }

    public JoZRuntimeException(Throwable throwable) {
        super(throwable);
    }
}
