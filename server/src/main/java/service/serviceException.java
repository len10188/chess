package service;

public class serviceException extends RuntimeException {
    public serviceException(String message) {
        super(message);
    }

    public static class AlreadyTakenException extends Exception{}
    public static class BadRequestException extends Exception{}
    public static class UnauthorizedException extends Exception{}
}
