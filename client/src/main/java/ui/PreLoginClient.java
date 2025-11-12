package ui;

public class PreLoginClient {
    private final ServerFacade facade;
    private final MessageHandler handler;
    private final String serverUrl;

    public PreLoginClient(String serverUrl, MessageHandler handler){
        this.facade = new ServerFacade(serverUrl);
        this.handler = handler;
        this.serverUrl = serverUrl;

    }
    public String getServerUrl() {
        return serverUrl;
    }

    public String welcome() {
        return "Welcome to Chess!";
    }

    public String help() {
        return """
        Commands:
         - register <USERNAME> <PASSWORD> <EMAIL> - to create an account
         - login <USERNAME> <PASSWORD> - to play chess
         - quit - playing chess
         - help - with possible commands
        """;
    }

    public String eval(String input) throws Exception {
        var parts = input.split("\\s+");
        if (parts.length == 0) return "";
        var cmd = parts[0].toLowerCase();

        return switch (cmd) {
            case "help" -> help();
            case "quit" -> "quit";
            case "register" -> {
                if (parts.length < 4) yield "Usage: register <username> <password> <email>";
                var token = facade.register(parts[1], parts[2], parts[3]);
                if (token != null) {
                    yield "success\n" + token;
                } else {
                    yield "Registration failed.";
                }
            }
            case "login" -> {
                if (parts.length < 3) yield "Usage: login <username> <password>";
                var token = facade.login(parts[1], parts[2]);
                if (token != null && !token.isBlank()) {
                    yield "success\n" + token;
                } else {
                    yield "Login failed.";
                }
            }
            default -> "Unknown command. Try 'help'.";
        };
    }
}
