package com.tumri.joz;

/**
 * Top level checked exception that gets thrown by the JoZ Server. All the implementation specific
 * checked exceptions should be encapsulated into this exception class, to facilitate client to catch only single top-level
 * checked exception within its code, if required. <Br/>
 * This exception hirearchy should only be used if the user can recover from the problem, else JoZRuntimeException should be
 * used instead.
 *
 * @author  bpatel
 * @since   1.0.0
 * @version 1.0.0
 */
public class JoZException extends Exception {

    public JoZException() {
    }

    public JoZException(String string) {
        super(string);
    }

    public JoZException(String string, Throwable throwable) {
        super(string, throwable);
    }

    public JoZException(Throwable throwable) {
        super(throwable);
    }
}
