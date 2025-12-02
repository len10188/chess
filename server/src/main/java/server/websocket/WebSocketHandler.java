package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import io.javalin.websocket.*;
import model.AuthData;
import model.GameData;
import org.jetbrains.annotations.NotNull;
import websocket.commands.UserGameCommand;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.io.IOException;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler, WsErrorHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final Gson gson = new Gson();

    public WebSocketHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    @Override
    public void handleClose(@NotNull WsCloseContext wsCloseContext) throws Exception {
        System.out.println("WebSocket closed");
    }

    @Override
    public void handleConnect(@NotNull WsConnectContext wsConnectContext) throws Exception {
        System.out.println("WebSocket connected");
        wsConnectContext.enableAutomaticPings();
    }

    @Override
    public void handleError(WsErrorContext ctx) {
        System.out.println("WebSocket error occurred");
        if (ctx.error() != null) {
            ctx.error().printStackTrace();
        }
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext wsMessageContext) throws Exception {
        try {
            //System.out.println("WS received raw: " + wsMessageContext.message());
            UserGameCommand command = gson.fromJson(wsMessageContext.message(), UserGameCommand.class);
            if (command == null || command.getCommandType() == null){
                sendError(wsMessageContext.session , "Invalid command");
                return;
            }

            //System.out.println("Parsed WS command: "+ command.getCommandType());

            switch (command.getCommandType()) {
                case CONNECT -> connect(command, wsMessageContext.session);
                case MAKE_MOVE -> makeMove(command, wsMessageContext.session);
                case LEAVE -> leave(command, wsMessageContext.session);
                case RESIGN -> resign(command, wsMessageContext.session);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                sendError(wsMessageContext.session , "Server error: " + ex.getMessage());
            } catch (IOException ignored) {
            }
        }
    }

    private void connect(UserGameCommand command, Session session) throws IOException, DataAccessException {
        //System.out.println("Handling CONNECT for gameID=" + command.getGameID());
        String authToken = command.getAuthToken();
        Integer gameID = command.getGameID();

        if (authToken == null || gameID == null){
            sendError(session, "Missing authToken or gameID for CONNECT");
            session.close();
            return;
        }

        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            sendError(session, "Unauthorized");
            session.close();
            return;
        }

        GameData gameData = gameDAO.getGame(gameID);
        if (gameData == null) {
            sendError(session,"Game not found");
            session.close();
            return;
        }

        // add session to game's connection set
        connections.add(gameID, session);

        //send game state to client
        LoadGameMessage loadMsg = new LoadGameMessage(gameData.game());
        String loadJson = gson.toJson(loadMsg);
        //System.out.println("Sending LOAD_GAME JSON: " + loadJson);
        session.getRemote().sendString(loadJson);

        String username = auth.username();
        String noteText;

        if (username.equals(gameData.whiteUsername())){
            noteText = username + " joined as white";
        } else if (username.equals(gameData.blackUsername())){
            noteText = username + " joined as black";
        } else {
            noteText = username + " joined as an observer";
        }
        NotificationMessage note = new NotificationMessage(noteText);
        String noteJson = gson.toJson(note);
        connections.broadcast(gameID, session, noteJson);

    }

    private void makeMove( UserGameCommand command, Session session) throws IOException, DataAccessException{

        String authToken = command.getAuthToken();
        Integer gameID = command.getGameID();
        if (authToken == null || gameID == null) {
            sendError(session, "Missing authToken or gameID for MAKE_MOVE");
            return;
        }

        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            sendError(session, "Unauthorized");
            return;
        }
        GameData gameData = gameDAO.getGame(gameID);
        if (gameData == null) {
            sendError(session, "Game not found");
            return;
        }
        if (gameData.game().isGameOver()) {
            sendError(session, "Game is already over");
            return;
        }

        String username = auth.username();
        String white = gameData.whiteUsername();
        String black = gameData.blackUsername();

        ChessGame.TeamColor currentTurn = gameData.game().getTeamTurn();

        // Check the user is player
        if (!username.equals(white) && !username.equals(black)) {
            sendError(session, "Observers cannot make moves. Please leave and join a new game to play.");
            return;
        }

        // Check if players turn.
        if (currentTurn == ChessGame.TeamColor.WHITE && !username.equals(white)) {
            sendError(session, "It is white's turn");
            return;
        }
        if (currentTurn == ChessGame.TeamColor.BLACK && !username.equals(black)) {
            sendError(session, "It is black's turn");
            return;
        }

        if (command.getMove() == null) {
            sendError(session, "No move provided");
            return;
        }

        ChessGame game = gameData.game();

        try{
            // make move
            game.makeMove(command.getMove());

            ChessGame.TeamColor movedColor = currentTurn;
            ChessGame.TeamColor opponent;
            if (movedColor == ChessGame.TeamColor.WHITE){
                opponent = ChessGame.TeamColor.BLACK;
            } else {
                opponent = ChessGame.TeamColor.WHITE;
            }

            StringBuilder extraNote = new StringBuilder();

            String oppositeUser;
            if (username.equals(gameData.whiteUsername())){
                oppositeUser = gameData.blackUsername();
            } else {
                oppositeUser = gameData.whiteUsername();
            }

            if (game.isInCheckmate(opponent)) {
                game.setGameOver(true);
                extraNote.append("\nGAME OVER: ").append(oppositeUser).append(" is in checkmate! ").append(username).append(" WINS!");
                // broadcast move notification to everyone else.
                String moveText = formatMove(command.getMove());
                NotificationMessage note = new NotificationMessage(
                        username + " played " + moveText + extraNote
                );
                String noteJson = gson.toJson(note);
                connections.broadcast(gameID, null, noteJson);
                return;


            } else if (game.isInStalemate(opponent)) {
                game.setGameOver(true);
                extraNote.append("\nGAME OVER: Stalemate. ");

                // broadcast move notification to everyone else.
                String moveText = formatMove(command.getMove());
                NotificationMessage note = new NotificationMessage(
                        username + " played " + moveText + extraNote
                );
                String noteJson = gson.toJson(note);
                connections.broadcast(gameID, null, noteJson);
                return;

            } else if (game.isInCheck(opponent)){
                extraNote.append(". ").append(oppositeUser).append(" is in check.");

                // broadcast move notification to everyone else.
                String moveText = formatMove(command.getMove());
                NotificationMessage note = new NotificationMessage(
                        username + " played " + moveText + extraNote
                );
                String noteJson = gson.toJson(note);
                connections.broadcast(gameID, null, noteJson);
                return;
            }
            // update gameboard
            gameDAO.updateGame(gameID, game);

            // broadcast update to everyone
            LoadGameMessage loadMsg = new LoadGameMessage(game);
            String loadJson = gson.toJson(loadMsg);
            connections.broadcast(gameID, null, loadJson);

            // broadcast move notification to everyone else.
            String moveText = formatMove(command.getMove());
            NotificationMessage note = new NotificationMessage(
                    username + " played " + moveText + extraNote
            );
            String noteJson = gson.toJson(note);
            connections.broadcast(gameID, session, noteJson);

        } catch (Exception e) {
            sendError(session, "Move failed: " + e.getMessage());
        }
    }

    private void leave(UserGameCommand command, Session session) throws IOException, DataAccessException {
        String authToken = command.getAuthToken();
        Integer gameID = command.getGameID();

        if (authToken == null || gameID == null) {
            sendError(session, "Missing authToken or gameID for LEAVE");
            session.close();
            return;
        }
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            sendError(session, "Unauthorized");
            session.close();
            return;
        }

        GameData gameData = gameDAO.getGame(gameID);
        if (gameData == null) {
            sendError(session, "Game not found");
            session.close();
            return;
        }

        String username = auth.username();

        // if player, clear from DB
        if (username.equals(gameData.whiteUsername())){
            gameDAO.updateGamePlayers(gameID, "white", null);
        } else if (username.equals(gameData.blackUsername())){
            gameDAO.updateGamePlayers(gameID, "black", null);
        }
        NotificationMessage note = new NotificationMessage(
                auth.username() + " left game " + gameID
        );
        String noteJson = gson.toJson(note);
        connections.broadcast(gameID, session, noteJson);


        connections.remove(gameID, session);
        session.close();
    }

    private void resign(UserGameCommand command, Session session) throws IOException, DataAccessException {
        String authToken = command.getAuthToken();
        Integer gameID = command.getGameID();


        if (authToken == null || gameID == null) {
            sendError(session, "Missing authToken or gameID for RESIGN");
            return;
        }

        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            sendError(session, "Unauthorized");
            return;
        }

        GameData gameData = gameDAO.getGame(gameID);
        if (gameData == null) {
            sendError(session, "Game not found");
            return;
        }

        String username = auth.username();
        // Check the user is player
        if (!username.equals(gameData.whiteUsername()) && !username.equals(gameData.blackUsername())) {
            sendError(session, "Observers cannot resign. Type 'leave' if you wish to return to the lobby.");
            return;
        }

        // set game over flag
        ChessGame game = gameData.game();
        if (game.isGameOver()) {
            sendError(session, "Game is already over");
            return;
        }

        game.setGameOver(true);
        gameDAO.updateGame(gameID, game); // update the game with new status.

        // Establish winner
        String winnerText = "";
        if (username.equals(gameData.whiteUsername())) {
            winnerText = "Black wins by resignation.";
        } else {
            winnerText = "White wins by resignation.";
        }

        // Notify everyone in the game
        String fullMessage = String.format(
                "GAME OVER: %s resigned from game %d. %s",
                username, gameID, winnerText
        );
        NotificationMessage note = new NotificationMessage(
                "Game over: " + username + " resigned from game " + gameID + ".\n" + winnerText
        );

        String noteJson = gson.toJson(note);
        connections.broadcast(gameID, null, noteJson);
    }


    private void  sendError (Session session, String message) throws IOException {
        ErrorMessage err = new ErrorMessage(message);
        String json = gson.toJson(err);
        if (session != null && session.isOpen()) {
            session.getRemote().sendString(json);
        }
    }
    private String formatSquare(chess.ChessPosition pos) {
        int col = pos.getColumn();
        int row = pos.getRow();

        char file = (char) ('a' + (col - 1));
        return "" + file + row;
    }

    private String formatMove(chess.ChessMove move) {
        String from = formatSquare(move.getStartPosition());
        String to = formatSquare(move.getEndPosition());

        if (move.getPromotionPiece() != null) {

            char promoChar = switch (move.getPromotionPiece()) {
                case QUEEN  -> 'q';
                case ROOK   -> 'r';
                case BISHOP -> 'b';
                case KNIGHT -> 'n';
                default     -> '?';
            };
            return from + " to " + to + " promoting to " + promoChar;
        } else {
            return from + " to " + to;
        }
    }
}

// both players recieve checkmate notification, include username of who is in checkmate.
// check should send to both.

// error index out of bounds.