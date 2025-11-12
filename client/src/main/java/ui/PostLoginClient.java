package ui;

import chess.ChessGame;
import model.GameData;
import java.util.*;

public class PostLoginClient {
    private final ServerFacade facade;
    private final MessageHandler handler;
    private String authToken;

    private final List<GameData> lastListedGames = new ArrayList<>();

    public PostLoginClient(String serverUrl, MessageHandler handler){
        this.facade = new ServerFacade(serverUrl);
        this.handler = handler;
        this.authToken = facade.authToken;
    }

    public String welcome() {
        return "Welcome! You are now logged in."; // UPDATE THIS LATER maybe add a randomized string afterwards?
    }

    public String help() {
        return """
        Options:
        List current games: 'l', 'list'
        Create a new game: 'c', 'create' <GAME NAME>
        Join a game: 'j', 'join', <GAME ID> <COLOR>
        Watch a game: 'w', 'watch' <GAME ID>
        Logout: 'logout'
        """;
    }

    public String logout() throws Exception {
        try {
            facade.logout();
            return "goodbye";
        } catch (Exception e) {
            return "Logout failed: " + e.getMessage();
        }
    }

    private String listGames() throws Exception {
        try {
            var games = facade.listGames();
            lastListedGames.clear();
            lastListedGames.addAll(games);

            if (games.isEmpty()) return "No games found. Try making one!";

            StringBuilder stringBuilder = new StringBuilder("Games: \n");
            int i = 1;
            for (var g : games) {
                stringBuilder.append(String.format(
                        "%d. %s (white: %s, black: %s)%n",
                        i++,
                        g.gameName(),
                        showPlayer(g.whiteUsername()),
                        showPlayer(g.blackUsername())
                ));
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            return "Could not list games: " + e.getMessage();
        }
    }

    private String createGame(String name) throws Exception {
        try {
            var game = facade.createGame(name);
            if (game == null) return "Game creation failed.";
            return "Created game: " +game.gameName();
        } catch (Exception e) {
            return "Create failed: " + e.getMessage();
        }
    }

    private String playGame (int gameIdNum, String color) throws Exception {
        var game = lastListedGames.get(gameIdNum - 1);
        try {
            if (!color.equals("white") && !color.equals("black")) {
                return "Color must be 'white' or 'black'.";
            }
            facade.joinGame(color, game.gameID());

            // draw board
            if (color.equals("white")){
                var board = PrintBoard.main(new chess.ChessGame(), ChessGame.TeamColor.WHITE);
            } else {
                var board = PrintBoard.main(new chess.ChessGame(), ChessGame.TeamColor.BLACK);
            }
            return "Joined game as " + color + ":\nBOARD\n" + board;
        } catch (Exception e) {
            return "join failed: " + e.getMessage();
        }
    }

    private String watchGame(int gameIdNum) throws Exception {
        var game = lastListedGames.get(gameIdNum - 1);
        try {
            facade.joinGame(null, game.gameID());

            var board = PrintBoard.main(new chess.ChessGame(), ChessGame.TeamColor.WHITE);
            return "Observing game: \nBOARD\n" + board;
        } catch (Exception e) {
            return "Watch game failed: " + e.getMessage();
        }
    }

    public String eval(String input) throws Exception {
        if (input == null || input.isBlank()) return "";

        var parts = input.split("\\s+");
        if (parts.length == 0) return "";
        var cmd = parts[0].toLowerCase(); // allow for capital letters

        return switch (cmd) {
            case "help", "h" -> help();
            case "logout" -> logout();
            case "quit", "q" -> "quit";
            case "create", "c" -> {
                if (parts.length < 2) yield "Too few arguments provided. Usage: create <game_name>";
                else if (parts.length > 2) yield  "Too many arguments provided. Usage: create <game_name>";
                else yield "Create game failed. :(";
            }
        }
    }

    // helper fun
}
