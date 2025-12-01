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
        InGameClient inGame = null;

        System.out.println(postLogin.welcome());
        System.out.println(postLogin.help());

        while (state == UiState.LOGGED_IN || state ==UiState.IN_GAME) {
            printPrompt();
            String line = readLine(scanner);
            if (line == null) {
                break;
            }

            try {
                if (state == UiState.LOGGED_IN) {
                    // --------LOBBY MODE ---------
                    var parts = line.trim().split("\\s+");
                    if (parts.length == 0 || parts[0].isBlank()) {
                        continue;
                    }
                    var cmd = parts[0].toLowerCase();

                    // ---- JOIN ----
                    if((cmd.equals("join") || cmd.equals("j"))) {
                        if (parts.length != 3) {
                            System.out.println("Usage: join <gameNumber> <white|black>");
                            continue;
                        }

                        int gameNum = parseInt(parts[1]);
                        String color = parts[2].toLowerCase();

                        List<GameData> games = postLogin.getLastListedGames();
                        if (gameNum < 1 || gameNum > games.size()) {
                            System.out.println("Invalid game number. Use 'list' to see games.");
                            continue;
                        }

                        if (!color.equals("white") && !color.equals("black")) {
                            System.out.println("Color must be 'white' or 'black'.");
                            continue;
                        }

                        GameData game = games.get(gameNum - 1);

                        boolean joined;
                        try {
                            joined = postLogin.getFacade().joinGame(color, game.gameID());
                        } catch (Exception ex) {
                            System.out.println("Join failed: " + ex.getMessage());
                            continue;
                        }
                        if (!joined) {
                            continue;
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
                        continue;
                    }

                    // ------ WATCH ------
                    if ((cmd.equals("watch") || cmd.equals("w"))) {
                        if (parts.length != 2) {
                            System.out.println("Usage: watch <gameNumber>");
                            continue;
                        }

                        int gameNum = parseInt(parts[1]);
                        List<GameData> games = postLogin.getLastListedGames();
                        if (gameNum < 1 || gameNum > games.size()) {
                            System.out.println("Invalid game number. Use 'list' to see games.");
                            continue;
                        }

                        GameData game = games.get(gameNum - 1);

                        try {
                            inGame  = new InGameClient(serverUrl,
                                    authToken,
                                    game.gameID(),
                                    ChessGame.TeamColor.WHITE);
                            state = UiState.IN_GAME;
                            System.out.println("Observing game.");
                            System.out.println(inGame.help());
                        } catch (Exception e) {
                            System.out.println("Failed to observer game: " + e.getMessage());
                        }
                        continue;
                    }

                    String out = postLogin.eval(line);
                    if (out == null || out.isBlank()) {
                        continue;
                    }

                    if ("goodbye".equals(out)) {
                        System.out.println("Logged out.");
                        state = UiState.LOGGED_OUT;
                        break;
                    }
                    // Postlogin needs to render board.
                    if (out.startsWith("BOARD")) {
                        System.out.println(out.substring("BOARD\n".length()));
                        continue;
                    }

                    // everything else
                    System.out.println(out);
                } else if (state == UiState.IN_GAME) {
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
            }catch(Throwable t){
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
}
