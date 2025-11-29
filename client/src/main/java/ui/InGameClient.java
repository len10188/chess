package ui;

import chess.*;
import io.javalin.http.HttpResponseException;
import ui.websocket.ServerMessageHandler;
import ui.websocket.WebSocketFacade;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

public class InGameClient implements ServerMessageHandler {

    private final ServerFacade facade;
    private final WebSocketFacade webSocket;

    private final String authToken;
    private final int gameID;
    private final ChessGame.TeamColor perspective;

    private ChessGame currentGame;

    public InGameClient(String serverUrl,
                        String authToken,
                        int gameID,
                        ChessGame.TeamColor perspective) throws Exception {
        this.authToken = authToken;
        this.gameID = gameID;
        this.perspective = perspective;

        this.facade = new ServerFacade(serverUrl);
        this.facade.authToken = authToken;

        this.webSocket = new WebSocketFacade(serverUrl);
    }

    public String help() {
        return """
        Options:
        Show this message: 'h', 'help'
        Redraw the board: 'redraw'
        Make a move (example: 'move e2 e4'): 'm', 'move' <from> <to>
        Promotion (example: 'move e7 e8 q'): 'm', 'move' <from> <to> <q|r|b|n>
        Leave game and return to lobby: 'l', 'leave'
        Resign the game: 'resign'
        """;
    }

    public String eval(String input) {
        if (input == null || input.isBlank()){
            return "";
        }

        var parts = input.trim().split("\\s+");
        var cmd = parts[0].toLowerCase();

        return switch (cmd) {
            case "help", "h" -> help();
            case "redraw", "r" -> redraw();
            case "move", "m" -> {
                if (parts.length < 3) {
                    yield "Too few arguments.\nUsage: move <from> <to> [q|r|b|n]";
                }
                if (parts.length > 4) {
                    yield "Too many arguments.\nUsage: move <from> <to> [q|r|b|n]";
                }
                String from = parts[1];
                String to = parts[2];
                String promotionPiece;
                if (parts.length == 4){
                    promotionPiece = parts[3];
                } else{
                    promotionPiece = null;
                }
                yield makeMove(from, to, promotionPiece);
            }
            case "leave", "l" -> leave();
            case "resign" -> resign();
            case "order66" -> {
                yield "It will be done my lord.";
            }
            default ->  "Unknown command. Try 'help'.";
        };
    }

    //command implementation

    private String redraw() {
        if (currentGame == null) {
            return "No game state yet. Waiting for server...";
        }
        String board = PrintBoard.render(currentGame.getBoard(), perspective);
        return "CHESS BOARD\n" + board;
    }

    private String makeMove(String from, String to, String promotion) {
        if (currentGame == null) {
            return "No game loaded yet.";
        }

        try {
            ChessPosition start = parsePosition(from);
            ChessPosition end = parsePosition(to);

            ChessPiece promotionPiece = null;
            if (promotion != null){
                promotionPiece = switch (promotion.toLowerCase()) {
                    case "q" -> ChessPiece.PieceType.QUEEN;
                    case "r" -> ChessPiece.PieceType.ROOK;
                    case "b" -> ChessPiece.PieceType.BISHOP;
                    case "n" -> ChessPiece.PieceType.KNIGHT;
                    default -> null;
                };
            }

            ChessMove move = new ChessMove(start, end, promotionPiece)
        };
    }

    @Override
    public void handleLoadGame(LoadGameMessage message) {

    }

    @Override
    public void handleNotification(NotificationMessage message) {

    }

    @Override
    public void handleError(ErrorMessage message) {

    }
}
