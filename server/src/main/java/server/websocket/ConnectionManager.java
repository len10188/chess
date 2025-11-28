package server.websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    private final Map<Integer, Set<Session>> connections = new ConcurrentHashMap<>();

    // add client webSocket session to a game
    public void add(Integer gameID, Session session) {
        connections.computeIfAbsent(gameID, id -> ConcurrentHashMap.newKeySet()).add(session);
    }

    // remove a client webSocket from a game
    public void remove(Integer gameID, Session session) {
        var set = connections.get(gameID);
        if (set == null) {
            return;
        }
        set.remove(session);
        if (set.isEmpty()) {
            connections.remove(gameID);
        }
    }

    // broadcast message to everyone
    public void broadcast(Integer gameID, Session excludeSession, String message) throws IOException {
        if (!connections.containsKey(gameID)){
            return;
        }
        for (Session s : connections.get(gameID)) {
            if (s != null && s.isOpen()) {
                if (excludeSession == null || !s.equals(excludeSession)) {
                    s.getRemote().sendString(message);
                }
            }
        }
    }

    public void broadcast(Integer gameID, String message) throws IOException {
        broadcast(gameID, null, message);
    }
}

