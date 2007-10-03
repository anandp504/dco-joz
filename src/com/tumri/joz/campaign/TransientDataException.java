package com.tumri.joz.campaign;

import com.tumri.joz.JoZException;

/**
 * This Exception gets thrown when some inconsistencies arise in transient data while making tspec-add, tspec-delete, or
 * inorp-mapping-delta requests.
 *
 * @author bpatel
 */
public class TransientDataException extends JoZException {

    public TransientDataException() {
    }

    public TransientDataException(String string) {
        super(string);
    }

    public TransientDataException(String string, Throwable throwable) {
        super(string, throwable);
    }

    public TransientDataException(Throwable throwable) {
        super(throwable);
    }
}
