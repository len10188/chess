package result;

public record RegisterResult(String username, String authToken, String message) {
    public RegisterResult(String message){
        this(null, null, message);
    }

    public RegisterResult(String username, String authToken) {
        this(username, authToken, null);
    }
}
