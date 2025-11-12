package ui;

import chess.ChessGame;
import model.GameData;
import java.util.*;

public class PostLoginClient {
    private final ServerFacade facade;
    private final MessageHandler handler;
    private String authToken;

    private final List<GameData> lastListedGames = new ArrayList<>();

    public PostLoginClient(String serverUrl, MessageHandler handler, String authToken){
        this.facade = new ServerFacade(serverUrl);
        this.facade.authToken = authToken;
        this.handler = handler;
        this.authToken = authToken;

    }

    public String welcome() {
        return "Welcome! You are now logged in."; // UPDATE THIS LATER maybe add a randomized string afterwards?
    }

    public String help() {
        return """
        Options:
        List current games: 'l', 'list'
        Create a new game: 'c', 'create' <GAME NAME>
        Join a game: 'j', 'join', <GAME ID> <WHITE|BLACK>
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
            if (games == null) {
                return "Could not list games. Try logging in again. ";
            }
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

    private String createGame(String gameName) throws Exception {
        try {
            String createdGameName = facade.createGame(gameName);
            return (createdGameName == null) ? "Game creation failed." :  "Created game: " + createdGameName;
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
            var board = DrawBoard.renderInitial(color.equals("white")
                    ? chess.ChessGame.TeamColor.WHITE
                    : chess.ChessGame.TeamColor.BLACK);
            return "Joined game as " + color + ":\nBOARD\n" + board;
        } catch (Exception e) {
            return "join failed: " + e.getMessage();
        }
    }

    private String watchGame(int gameIdNum) throws Exception {
        var game = lastListedGames.get(gameIdNum - 1);
        try {
            facade.joinGame(null, game.gameID());

            var board = DrawBoard.renderInitial(ChessGame.TeamColor.WHITE);
            return "Observing game: \nBOARD\n" + board;
        } catch (Exception e) {
            return "Watch game failed: " + e.getMessage();
        }
    }

    private String resetDatabase() {
        try {
            facade.clear(); // uses your existing ServerFacade.clear()
            return "Database reset successful.";
        } catch (Exception e) {
            return "Reset failed: " + e.getMessage();
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
            case "list", "l" -> listGames();
            case "create", "c" -> {
                if (parts.length < 2) yield "Too few arguments provided. Usage: create <game_name>";
                else if (parts.length > 2) yield  "Too many arguments provided. Usage: create <game_name>";
                yield createGame(parts[1]);
            }
            case "join", "j" -> {
                if (parts.length < 3 ) yield "Too few arguments provided. Usage: join <number> <while|black>";
                if (parts.length > 3 ) yield "Too many arguments provided. Usage: join <number> <while|black>";
                int gameNum = parseInt(parts[1]);
                if (gameNum < 1 || gameNum > lastListedGames.size()) {
                    yield "Invalid game number. Use 'list' to see games.";
                }
                String color = parts[2].toLowerCase();
                yield playGame(gameNum, color);
            }
            case "watch", "w" -> {
                if (parts.length < 2) yield "Too few arguments provided. Usage: watch <number>";
                int gameNum = parseInt(parts[1]);
                if (gameNum < 1 || gameNum > lastListedGames.size()) {
                    yield "Invalid game number. Use 'list' to see games.";
                }
                yield watchGame(gameNum);
            }
            case "reset" -> resetDatabase();

            default -> "Unknown command. Try 'help'.";
        };
    }

    // helper funcs
    private int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String showPlayer(String name) {
        if (name == null || name.isBlank()){
            return "-";
        } else {
            return name;
        }
    }
}
