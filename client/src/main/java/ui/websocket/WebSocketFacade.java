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
import java.util.function.Consumer;

public class WebSocketFacade extends Endpoint{
    private final Gson gson = new Gson();

    private Session session;

    private final String authToken;
    private final int gameID;

    private final Consumer<LoadGameMessage> onLoadGame;
    private final Consumer<NotificationMessage> onNotification;
    private final Consumer<ErrorMessage> onError;

    public WebSocketFacade(
            String serverUrl,
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

        String webSocketUrl = serverUrl.replaceFirst("^http", "ws") + "/ws";
        System.out.println("Connecting to WebSocker URL: " + webSocketUrl);

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, new URI(webSocketUrl));

        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                handleIncoming(message);
            }
        });
    }

    @Override
    public void onOpen(Session session,EndpointConfig config) {
        System.out.println("WebSocket connected.");
        this.session = session;
        sendConnect();
    }

    private void handleIncoming(String json) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

     // SEND COMMANDS
    private void sendRaw(String json) {
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendText(json);
        } else {
            System.out.println("WebSocket session is not open; cannot send: " + json);
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
