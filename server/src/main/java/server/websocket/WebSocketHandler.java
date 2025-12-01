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
        if (ctx.error() != null) ctx.error().printStackTrace();
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext wsMessageContext) throws Exception {
        try {
            UserGameCommand command = gson.fromJson(wsMessageContext.message(), UserGameCommand.class);
            if (command == null || command.getCommandType() == null){
                sendError(wsMessageContext.session , "Error: Invalid command");
                return;
            }

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
        String authToken = command.getAuthToken();
        Integer gameID = command.getGameID();

        if (authToken == null || gameID == null){
            sendError(session, "Error: Missing authToken or gameID for CONNECT");
            session.close();
            return;
        }

        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            sendError(session, "Error: Unauthorized");
            session.close();
            return;
        }

        GameData gameData = gameDAO.getGame(gameID);
        if (gameData == null) {
            sendError(session,"Error: Game not found");
            session.close();
            return;
        }

        // add session to game's connection set
        connections.add(gameID, session);

        //send game state to client
        LoadGameMessage loadMsg = new LoadGameMessage(gameData.game());
        String loadJson = gson.toJson(loadMsg);
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
            sendError(session, "Error: Missing authToken or gameID for MAKE_MOVE");
            return;
        }

        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            sendError(session, "Error: Unauthorized");
            return;
        }
        GameData gameData = gameDAO.getGame(gameID);
        if (gameData == null) {
            sendError(session, "Error: Game not found");
            return;
        }
        if (gameData.game().isGameOver()) {
            sendError(session, "Error: Game is already over");
            return;
        }


        String username = auth.username();
        String white = gameData.whiteUsername();
        String black = gameData.blackUsername();

        ChessGame.TeamColor currentTurn = gameData.game().getTeamTurn();

        // Check the user is player
        if (!username.equals(white) && !username.equals(black)) {
            sendError(session, "Error: Observers cannot make moves. Please leave and join a new game to play.");
            return;
        }

        // Check if players turn.
        if (currentTurn == ChessGame.TeamColor.WHITE && !username.equals(white)) {
            sendError(session, "Error: It is white's turn");
            return;
        }
        if (currentTurn == ChessGame.TeamColor.BLACK && !username.equals(black)) {
            sendError(session, "Error: It is black's turn");
            return;
        }

        if (command.getMove() == null) {
            sendError(session, "Error: No move provided");
            return;
        }



        ChessGame game = gameData.game();

        try{



            ChessGame.TeamColor movedColor = currentTurn;
            ChessGame.TeamColor opponent;
            if (movedColor == ChessGame.TeamColor.WHITE){
                opponent = ChessGame.TeamColor.BLACK;
            } else {
                opponent = ChessGame.TeamColor.WHITE;
            }

            StringBuilder extraNote = new StringBuilder();

            if (game.isInCheckmate(opponent)) {
                game.setGameOver(true);
                extraNote.append(" Checkmate! ");
            } else if (game.isInStalemate(opponent)) {
                game.setGameOver(true);
                extraNote.append(" Stalemate. ");
            } else if (game.isInCheck(opponent)){
                extraNote.append(" ").append(opponent == ChessGame.TeamColor.WHITE ? "White" : "Black").append(" is in check.");
            }

            // make move
            game.makeMove(command.getMove());

            // update gameboard
            gameDAO.updateGame(gameID, game);

            // broadcast update to everyone
            LoadGameMessage loadMsg = new LoadGameMessage(game);
            String loadJson = gson.toJson(loadMsg);
            connections.broadcast(gameID, null, loadJson);

            // broadcast move notification
            NotificationMessage note = new NotificationMessage(
                    username + " played " + command.getMove() + extraNote
            );
            String noteJson = gson.toJson(note);
            connections.broadcast(gameID, null, noteJson);

        } catch (Exception e) {
            sendError(session, "Error: Move failed: " + e.getMessage());
        }
    }

    private void leave(UserGameCommand command, Session session) throws IOException, DataAccessException {
        String authToken = command.getAuthToken();
        Integer gameID = command.getGameID();

        if (authToken == null || gameID == null) {
            sendError(session, "Error: Missing authToken or gameID for LEAVE");
            session.close();
            return;
        }
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            sendError(session, "Error: Unauthorized");
            session.close();
            return;
        }

        GameData gameData = gameDAO.getGame(gameID);
        if (gameData == null) {
            sendError(session, "Error: Game not found");
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
            sendError(session, "Error: Missing authToken or gameID for RESIGN");
            return;
        }

        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            sendError(session, "Error: Unauthorized");
            return;
        }

        GameData gameData = gameDAO.getGame(gameID);
        if (gameData == null) {
            sendError(session, "Error: Game not found");
            return;
        }



        // set game over flag
        ChessGame game = gameData.game();
        if (game.isGameOver()) {
            sendError(session, "Error: Game is already over");
            return;
        }

        game.setGameOver(true);
        gameDAO.updateGame(gameID, game); // update the game with new status.

        // Notify everyone in the game
        NotificationMessage note = new NotificationMessage(
                auth.username() + " resigned from game " + gameID
        );

        String noteJson = gson.toJson(note);
        connections.broadcast(gameID, null, noteJson);

        // send final board status
        LoadGameMessage loadMsg = new LoadGameMessage(gameData.game());
        String loadJson = gson.toJson(loadMsg);
        connections.broadcast(gameID, null, loadJson);
    }


    private void  sendError (Session session, String message) throws IOException {
        ErrorMessage err = new ErrorMessage(message);
        String json = gson.toJson(err);
        if (session != null && session.isOpen()) {
            session.getRemote().sendString(json);
        }
    }
}
