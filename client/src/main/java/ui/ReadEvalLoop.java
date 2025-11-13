package ui;

import java.util.Scanner;

public class ReadEvalLoop implements MessageHandler {
    private final String serverUrl;
    private final PreLoginClient preLogin;

    private enum UiState {LOGGED_OUT, LOGGED_IN}
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
                if (line == null) break; // EOF reached.

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
                    System.out.println("Error: " + t.getMessage());
                }
            }
        }
    }

    private void runPostLogin(Scanner scanner, String authToken) {
        PostLoginClient postLogin = new PostLoginClient(serverUrl, this, authToken);

        System.out.println(postLogin.welcome());
        System.out.println(postLogin.help());

        while (state == UiState.LOGGED_IN) {
            printPrompt();
            String line = readLine(scanner);
            if (line == null) break;

            try {
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
            } catch (Throwable t) {
                System.out.println("Error: " + t.getMessage());
            }
        }
    }

    private void printPrompt() {
        String tag;
        if (state == UiState.LOGGED_OUT) {
            tag = "[LOGGED OUT]";
        } else {
            tag = "[LOGGED IN]";
        }
        System.out.println(tag +" >>> ");
    }

    private String readLine(Scanner scanner) {
        try {
            return scanner.nextLine();
        } catch (Exception e) {
            return null; //  EOF or closed.
        }
    }
}
