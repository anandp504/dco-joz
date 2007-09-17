package com.tumri.joz.campaign;

import com.tumri.joz.JoZException;

/**
 * This exception gets thrown if the OSpec name specified by the client is not present in JoZ in-memory DB.
 *
 * @author  bpatel
 */
public class OSpecNotFoundException extends JoZException {

    public OSpecNotFoundException() {
    }

    public OSpecNotFoundException(String string) {
        super(string);
    }

    public OSpecNotFoundException(String string, Throwable throwable) {
        super(string, throwable);
    }

    public OSpecNotFoundException(Throwable throwable) {
        super(throwable);
    }
}
