package com.ongyx.cassette.exception;

/**
 * Base class for custom exceptions in Cassette.
 * @author Ong Yong Xin
 */
public class CassetteException extends RuntimeException {
    public CassetteException() {
    }

    public CassetteException(String msg) {
        super(msg);
    }
}
