package server.websocket;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageHandler;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final AuthDAO authDAO;
    private final GameService gameService;
    private final Gson gson = new Gson();

}
