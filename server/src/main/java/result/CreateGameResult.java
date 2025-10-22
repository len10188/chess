package result;

public record CreateGameResult (Integer gameID, String message) {
    public CreateGameResult(Integer gameID) {
        this(gameID, null);
    }
    public CreateGameResult(String message){
        this(null, message);
    }
}
