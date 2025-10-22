package service;

public class ServiceException extends RuntimeException {
    public ServiceException(String message) {
        super(message);
    }

    public static class AlreadyTakenException extends Exception{}
    public static class BadRequestException extends Exception{}
    public static class UnauthorizedException extends Exception{}
}
