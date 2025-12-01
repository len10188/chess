package ui;

import java.util.List;
import java.util.Scanner;

import chess.ChessGame;
import model.GameData;

public class ReadEvalLoop implements MessageHandler {
    private final String serverUrl;
    private final PreLoginClient preLogin;

    private enum UiState {LOGGED_OUT, LOGGED_IN, IN_GAME}
    private UiState state = UiState.LOGGED_OUT;

    private InGameClient inGame;


    public ReadEvalLoop(String serverUrl) {
        this.serverUrl = serverUrl;
        this.preLogin = new PreLoginClient(serverUrl, this);
    }

    public void run() {
        System.out.println(preLogin.welcome());
        System.out.println(preLogin.help());

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                printPrompt();
                String line = readLine(scanner);
                if (line == null){
                    break; // EOF reached.
                }

                try {
                    String result = preLogin.eval(line);

                    // QUIT COMMAND
                    if ("quit".equals(result)) {
                        System.out.println("Goodbye!");
                        break;
                    }

                    // LOGIN or REGISTER successful -> go to post login
                    if (result != null && result.startsWith("success!\n")){
                        String token = result.substring("success!\n".length());
                        state = UiState.LOGGED_IN;
                        runPostLogin(scanner, token);
                        continue;
                    }

                    // Else print what prelogin said
                    if (result != null && !result.isBlank()) {
                        System.out.println(result);
                    }
                } catch (Throwable t) {
                    System.out.println("Error: " + t.getClass().getSimpleName());
                }
            }
        }
    }

    private void runPostLogin(Scanner scanner, String authToken) {
        PostLoginClient postLogin = new PostLoginClient(serverUrl, this, authToken);

        System.out.println(postLogin.welcome());
        System.out.println(postLogin.help());

        while (state == UiState.LOGGED_IN || state == UiState.IN_GAME) {
            printPrompt();
            String line = readLine(scanner);
            if (line == null) {
                break;
            }

            try {
                if (state == UiState.LOGGED_IN) {
                    handleLoggedInLine(line, postLogin, authToken);
                } else if (state == UiState.IN_GAME) {
                    handleInGameLine(line, postLogin);
                }
            } catch (Throwable t) {
                System.out.println("Error: " + t.getClass().getSimpleName());
            }
        }
    }
    private void printPrompt() {
        String tag;
        switch(state){
            case LOGGED_IN -> tag = "[LOGGED IN]";
            case LOGGED_OUT -> tag = "[LOGGED OUT]";
            case IN_GAME -> tag = "[IN GAME]";
            default -> tag =  "[ERROR IN GAME STATE]";
        }

        if (state == UiState.LOGGED_IN || state == UiState.LOGGED_OUT) {
            System.out.println(tag + " >>> ");
        }
    }

    private String readLine(Scanner scanner) {
        try {
            return scanner.nextLine();
        } catch (Exception e) {
            return null; //  EOF or closed.
        }
    }
    private int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void handleLoggedInLine(String line, PostLoginClient postLogin, String authToken) {
        var parts = line.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isBlank()) {
            return;
        }
        var cmd = parts[0].toLowerCase();

        // JOIN
        if (cmd.equals("join") || cmd.equals("j")) {
            handleJoinCommand(parts, postLogin, authToken);
            return;
        }

        // watch
        if (cmd.equals("watch") || cmd.equals("w")) {
            handleWatchCommand(parts, postLogin, authToken);
            return;
        }

        handlePostLoginEval(line, postLogin);
    }

    private void handleJoinCommand(String[] parts, PostLoginClient postLogin, String authToken){
        if (parts.length != 3) {
            System.out.println("Usage: join <gameNumber> <white|black>");
            return;
        }

        int gameNum = parseInt(parts[1]);
        String color = parts[2].toLowerCase();

        List<GameData> games = postLogin.getLastListedGames();
        if (gameNum < 1 || gameNum > games.size()) {
            System.out.println("Invalid game number. Use 'list' to see games.");
            return;
        }

        GameData game = games.get(gameNum - 1);

        boolean joined;
        try {
            joined = postLogin.getFacade().joinGame(color, game.gameID());
        } catch (Exception ex) {
            System.out.println("Join failed: " + ex.getMessage());
            return;
        }
        if (!joined) {
            return;
        }

        ChessGame.TeamColor perspective;
        if ("white".equalsIgnoreCase(color)) {
            perspective = ChessGame.TeamColor.WHITE;
        } else {
            perspective = ChessGame.TeamColor.BLACK;
        }

        try {
            inGame = new InGameClient(serverUrl, authToken, game.gameID(), perspective);
            state = UiState.IN_GAME;
            System.out.println("Joined game as " + color + ".");
            System.out.println(inGame.welcome());
            System.out.println(inGame.help());
        } catch (Exception e) {
            System.out.println("Failed to enter game: " + e.getMessage());
        }
    }

    private void handlePostLoginEval(String line, PostLoginClient postLogin) {
        String out = postLogin.eval(line);
        if (out == null || out.isBlank()) {
            return;
        }

        if ("goodbye".equals(out)) {
            System.out.println("Logged out.");
            state = UiState.LOGGED_OUT;
            return;
        }

        if (out.startsWith("BOARD")) {
            System.out.println(out.substring("BOARD\n".length()));
            return;
        }

        System.out.println(out);
    }

    private void handleWatchCommand(String[] parts, PostLoginClient postLogin, String authToken) {
        if (parts.length != 2) {
            System.out.println("Usage: watch <gameNumber>");
            return;
        }

        int gameNum = parseInt(parts[1]);
        List<GameData> games = postLogin.getLastListedGames();
        if (gameNum < 1 || gameNum > games.size()) {
            System.out.println("Invalid game number. Use 'list' to see games.");
            return;
        }

        GameData game = games.get(gameNum - 1);

        try {
            inGame = new InGameClient(
                    serverUrl,
                    authToken,
                    game.gameID(),
                    ChessGame.TeamColor.WHITE   // observers see from white perspective
            );
            state = UiState.IN_GAME;
            System.out.println("Observing game.");
            System.out.println(inGame.help());
        } catch (Exception e) {
            System.out.println("Failed to observe game: " + e.getMessage());
        }
    }


    private void handleInGameLine(String line, PostLoginClient postLogin) {
        if (inGame == null) {
            System.out.println("Error: Not currently in a game.");
            state = UiState.LOGGED_IN;
            return;
        }

        String out = inGame.eval(line);
        if (out != null && !out.isBlank()) {
            System.out.println(out);
        }

        if (out != null && out.startsWith("Leaving game.")) {
            state = UiState.LOGGED_IN;
            inGame = null;
            System.out.println(postLogin.help());
        }

    }
}
