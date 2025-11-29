package ui.websocket;

import com.google.gson.Gson;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import javax.websocket.Session;
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

        WebSocketClient client = WebsocketClient();
        client.start();

        URI uri = URI.create(serverUrl);
        Future<Session> future = client.connect(this, uri);
        this.session = future.get();
    }
}
