package com.tumri.joz.client;

public class JoZClientException extends Exception {

    public JoZClientException() {
    }

    public JoZClientException(String string) {
        super(string);
    }

    public JoZClientException(String string, Throwable throwable) {
        super(string, throwable);
    }

    public JoZClientException(Throwable throwable) {
        super(throwable);
    }
}
