package dataaccess;

import model.GameData;

import java.sql.SQLException;
import java.util.Collection;

public interface GameDAO {
    GameData createGame(String gameName) throws SQLException;
    GameData getGame(int id);
    Collection<GameData> listGames();
    void updateGamePlayers(int id, String playerColor, String username);
    void clear();
}
