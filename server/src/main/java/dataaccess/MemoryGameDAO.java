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
    public void updateGamePlayers(int id, String playerColor, String username) {
        GameData oldGame = gamesList.get(id);
        if (oldGame == null) {
            throw new IllegalArgumentException("Game with ID " + id + " doesn't exist");
        }

        GameData updatedGame;

        if ("white".equalsIgnoreCase(playerColor)) {
            if (oldGame.whiteUsername() != null) {
                throw new IllegalStateException("White player slot is already taken");
            }
            updatedGame = new GameData(
                    oldGame.gameID(),
                    username, // new white player username
                    oldGame.blackUsername(),
                    oldGame.gameName(),
                    oldGame.game()
            );
        } else if ("black".equalsIgnoreCase(playerColor)) {
            if (oldGame.blackUsername() != null) {
                throw new IllegalStateException("Black player slot is already taken");
            }
            updatedGame = new GameData(
                    oldGame.gameID(),
                    oldGame.whiteUsername(),
                    username, // new black player username
                    oldGame.gameName(),
                    oldGame.game()
            );
        } else {
            throw new IllegalArgumentException("Invalid player color: " + playerColor);
        }

        //replace old gamedata with new game data.
        gamesList.put (id, updatedGame);
    }


    @Override
    public void clear() {
        gamesList.clear();
        nextId.set(1);
    }
}
