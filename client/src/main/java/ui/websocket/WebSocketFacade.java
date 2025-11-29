package ui.websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import jakarta.websocket.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.awt.*;
import java.net.URI;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class WebSocketFacade {
    private final Gson gson = new Gson();

    private Session session;

    private final String authToken;
    private final int gameID;

    private final Consumer<LoadGameMessage> onLoadGame;
    private final Consumer<NotificationMessage> onNotification;
    private final Consumer<ErrorMessage> onError;

    public WebSocketFacade(
            String webSocketUrl,
            String authToken,
            int gameID,
            Consumer<LoadGameMessage> onLoadGame,
            Consumer<NotificationMessage> onNotification,
            Consumer<ErrorMessage> onError
    ) throws Exception {
        this.authToken = authToken;
        this.gameID = gameID;
        this.onLoadGame = onLoadGame;
        this.onNotification = onNotification;
        this.onError = onError;

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, URI.create(webSocketUrl));

    }

    // HANDLE EVENTS
     @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("WebSocket connected.");

         // connect automatically
        sendConnect();
     }

     @OnMessage
    public void onMessage(String json) {
         ServerMessage base = gson.fromJson(json, ServerMessage.class);

         switch (base.getServerMessageType()) {
             case LOAD_GAME -> {
                 LoadGameMessage msg = gson.fromJson(json, LoadGameMessage.class);
                 onLoadGame.accept(msg);
             }
             case NOTIFICATION -> {
                 NotificationMessage msg = gson.fromJson(json, NotificationMessage.class);
                 onNotification.accept(msg);
             }
             case ERROR -> {
                 ErrorMessage msg = gson.fromJson(json, ErrorMessage.class);
                 onError.accept(msg);
             }
         }
     }

     @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("WebSocket closed: " + reason);
        this.session = null;
     }

     @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("WebSocket error: " + throwable.getMessage());
     }

     // SEND COMMANDS
    private void sendRaw(String json) {
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendText(json);
        }
    }

    private void sendConnect() {
        UserGameCommand cmd = new UserGameCommand(
                UserGameCommand.CommandType.CONNECT,
                authToken,
                gameID,
                null
        );
        sendRaw(gson.toJson(cmd));
    }

    public void sendMove(ChessMove move) {
        UserGameCommand cmd = new UserGameCommand(
                UserGameCommand.CommandType.MAKE_MOVE,
                authToken,
                gameID,
                move
        );
        sendRaw(gson.toJson(cmd));
    }

    public void sendLeave() {
        UserGameCommand cmd = new UserGameCommand(
        UserGameCommand.CommandType.LEAVE,
                authToken,
                gameID,
                null
        );
        sendRaw(gson.toJson(cmd));
    }

    public void sendResign() {
        UserGameCommand cmd = new UserGameCommand(
                UserGameCommand.CommandType.RESIGN,
                authToken,
                gameID,
                null
        );
        sendRaw(gson.toJson(cmd));
    }
}
