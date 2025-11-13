package ui;

public class ServiceException extends RuntimeException {
    public ServiceException(String message) {
        super(message);
    }

    public static class AlreadyTakenException extends Exception {
    }
}