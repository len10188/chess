package dataaccess;

import model.GameData;

import java.util.Collection;

public interface GameDAO {
    GameData createGame(String gameName);
    GameData getGame(int id);
    Collection<GameData> listGames();
    void updateGame(GameData games);
    void updateGamePlayers(int id, String playerColor, String username);
    void clear();
}
