package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoryGameDAO implements GameDAO{

    private final Map<Integer, GameData> gamesList = new HashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    @Override
    public GameData createGame(String gameName) {
        // get current id and increment
        int id = nextId.getAndIncrement();

        // Initialize a new chess game
        ChessGame chessGame = new ChessGame();

        GameData game = new GameData(id, null, null, gameName, chessGame);
        gamesList.put(id, game);
        return game;
    }

    @Override
    public GameData getGame(int id) {
        return gamesList.get(id);
    }

    @Override
    public Collection<GameData> listGames() {
        return new ArrayList<>(gamesList.values());
    }

    @Override
    public void updateGame(GameData games) {
        gamesList.put(games.gameID(), games);
    }

    @Override
    public void clear() {
        gamesList.clear();
        nextId.set(1);
    }
}
