package ui;

import chess.*;
import ui.websocket.ServerMessageHandler;
import ui.websocket.WebSocketFacade;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

public class InGameClient implements ServerMessageHandler {

    private final WebSocketFacade webSocket;

    private final String authToken;
    private final int gameID;
    private final ChessGame.TeamColor perspective;
    private boolean waitingForResignConfirm = false;

    private ChessGame currentGame;

    public InGameClient(String serverUrl,
                        String authToken,
                        int gameID,
                        ChessGame.TeamColor perspective) throws Exception {
        this.authToken = authToken;
        this.gameID = gameID;
        this.perspective = perspective;

        ServerFacade facade = new ServerFacade(serverUrl);
        facade.authToken = authToken;

        this.webSocket = new WebSocketFacade(
                serverUrl,
                authToken,
                gameID,
                this::handleLoadGame,
                this::handleNotification,
                this::handleError
        );
    }
    public String welcome() {
        return "\n♕♕♕ You are now playing chess! ♕♕♕\n";
    }

    public String help() {
        return """
        --------Help Menu--------
        Options:
          Show this message: 'h', 'help'
          Redraw the board: 'redraw'
          Highlight legal moves (example: 'highlight e2'): 'hl', 'highlight' <square>
          Make a move (example: 'move e2 e4'): 'm', 'move' <from> <to>
          Promotion (example: 'move e7 e8 q'): 'm', 'move' <from> <to> <q|r|b|n>
          Leave game and return to lobby: 'l', 'leave'
          Resign the game: 'resign'
        -------------------------
        """;
    }

    public String eval(String input) {
        if (input == null || input.isBlank()){
            return "";
        }

        if (waitingForResignConfirm){
            String answer = input.trim().toLowerCase();
            waitingForResignConfirm = false; // reset to false

            return switch (answer) {
                case "y", "yes" -> {
                    resign();
                    yield "";
                }
                case "n", "no" -> "Resign canceled.";

                default -> {
                    yield "Please answer 'y' or 'n'. Resign Canceled";
                }
            };
        }

        var parts = input.trim().split("\\s+");
        var cmd = parts[0].toLowerCase();

        return switch (cmd) {
            case "help", "h" -> help();
            case "redraw", "r" -> redraw();
            case "highlight", "hl" -> {
                if (parts.length != 2) {
                    yield "Usage: highlight <square>";
                }
                yield highlightMoves(parts[1]);
            }
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
            case "resign" -> {
                waitingForResignConfirm = true;
                yield "Are you sure you want to resign? (y/n)";
            }
            case "order66" -> "It will be done my lord.";
            default ->  "Unknown command. Try 'help'.";
        };
    }

    //command implementation

    private String redraw() {
        if (currentGame == null) {
            return "No game state yet. Waiting for server...";
        }
        String board = PrintBoard.render(currentGame.getBoard(), perspective);
        return "            CHESS BOARD\n" + board;
    }

    private String makeMove(String from, String to, String promotionName) {
        if (currentGame == null) {
            return "No game loaded yet.";
        }

        try {
            ChessPosition start = parsePosition(from);
            ChessPosition end = parsePosition(to);

            ChessPiece.PieceType promotionPiece = null;
            if (promotionName != null){
                promotionPiece = switch (promotionName.toLowerCase()) {
                    case "q" -> ChessPiece.PieceType.QUEEN;
                    case "r" -> ChessPiece.PieceType.ROOK;
                    case "b" -> ChessPiece.PieceType.BISHOP;
                    case "n" -> ChessPiece.PieceType.KNIGHT;
                    default -> null;
                };
            }

            ChessMove move = new ChessMove(start, end, promotionPiece);

            webSocket.sendMove(move);
            if (promotionPiece == null) {
                return "Attempting move: " + from + " -> "+ to;
            } else {
                return "Attempting move: " + from + " -> "+ to + "promoting to " + promotionPiece;
            }
        } catch (IllegalArgumentException e) {
            return "Invalid square given. Example: a1, e7, etc.";
        }
    }

    private String leave() {
        webSocket.sendLeave();
        return "Leaving game. Returning to lobby...";
    }

    private void resign() {
        webSocket.sendResign();
    }

    @Override
    public void handleLoadGame(LoadGameMessage message) {
        //System.out.println("handleLoadGame fired! in InGameClient");
        this.currentGame = message.getGame();
        String board = PrintBoard.render(currentGame.getBoard(), perspective);
        System.out.println("            CHESS BOARD\n"+ board);
    }

    @Override
    public void handleNotification(NotificationMessage message) {
        System.out.println(message.getMessage());
    }

    @Override
    public void handleError(ErrorMessage message) {
        System.out.println("ERROR: " + message.getErrorMessage());
    }

    private ChessPosition parsePosition(String s) {
        if (s == null || s.length() != 2) {
            throw new IllegalArgumentException("Bad square: " + s);
        }
        char file = Character.toLowerCase(s.charAt(0));
        char rank = s.charAt(1);
        int col = file - 'a' + 1;
        int row = rank - '0';
        return new ChessPosition(row, col);
    }

    public String getAuthToken() {
        return authToken;
    }

    public int getGameID() {
        return gameID;
    }

    private String highlightMoves(String from) {
        if (currentGame == null) {
            return "No game loaded yet.";
        }

        try {
            ChessPosition start = parsePosition(from);

            ChessPiece piece = currentGame.getBoard().getPiece(start);
            if (piece == null) {
                return "No piece at " + from.toLowerCase() + ".";
            }

            var legalMoves = currentGame.validMoves(start);

            if (legalMoves == null || legalMoves.isEmpty()){
                String board = PrintBoard.renderHighlighted(currentGame.getBoard(), perspective, start, legalMoves);
                return "No legal moves for " + from.toLowerCase() + ".\n" + board;
            }

            String board = PrintBoard.renderHighlighted(currentGame.getBoard(), perspective, start, legalMoves);
            return "Legal moves for " + from.toLowerCase() +":\n" + board;
        } catch (IllegalArgumentException e ) {
            return "Invalid square given. Example: a1, e7, etc.";
        }
    }
}
