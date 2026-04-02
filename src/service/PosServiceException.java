package service;

public class PosServiceException extends Exception {
    public PosServiceException(String message) { super(message); }
    public PosServiceException(String message, Throwable cause) { super(message, cause); }
}