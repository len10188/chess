package ui.websocket;

import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

public interface ServerMessageHandler {
    void handleLoadGame(LoadGameMessage message);
    void handleNotification(NotificationMessage message);
    void handleError(ErrorMessage message);
}
