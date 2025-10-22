package result;

public record JoinGameResult(String message) {
    public JoinGameResult(){
        this(null);
    }
}
