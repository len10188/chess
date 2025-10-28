package dataaccess;

import model.GameData;

import java.sql.SQLException;
import java.util.Collection;

public interface GameDAO {
    GameData createGame(String gameName) throws SQLException, DataAccessException;
    GameData getGame(int id) throws DataAccessException;
    Collection<GameData> listGames() throws DataAccessException;
    void updateGamePlayers(int id, String playerColor, String username);
    void clear();
}
