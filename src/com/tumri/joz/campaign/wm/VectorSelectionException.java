package com.tumri.joz.campaign.wm;

/**
 * This class gets thrown when there is a problem targeting a vector
 *
 * @author nipun
 */
public class VectorSelectionException extends Exception {

    public VectorSelectionException() {
    }

    public VectorSelectionException(String message) {
        super(message);
    }

    public VectorSelectionException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public VectorSelectionException(Throwable throwable) {
        super(throwable);
    }
}