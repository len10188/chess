package ui.request;

public record JoinGameRequest(String authToken, String playerColor, int gameID) {}

