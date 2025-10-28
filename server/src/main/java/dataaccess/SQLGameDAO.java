package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import service.ServiceException;

import java.sql.*;
import java.util.*;

public class SQLGameDAO implements GameDAO {

    private final Gson gson = new Gson();

    private final String[] createStatement = {
            """
            CREATE TABLE IF NOT EXISTS games (
            gameID INT NOT NULL AUTH_INCREMENT,
            whiteUsername VARCHAR(255) DEFAULT NULL,
            blackUsername VARCHAR(255) DEFAULT NULL,
            gameName VARCHAR(255) NOT NULL,
            game TEXT NOT NULL, 
            PRIMARY KEY (gameID),
            INDEX(gameName)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
"""
    };

    public SQLGameDAO() throws DataAccessException {
        configureDatabase();
    }

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createStatement){
                try (var preparedStatement = conn.prepareStatement(statement)){
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to configure game table", e);
        }
    }

    @Override
    public GameData createGame(String gameName) throws DataAccessException, SQLException {
        ChessGame chessGame = new ChessGame();
        var json = gson.toJson(chessGame);

        String sql = "INSERT INTO game (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
        var statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setNull(1, Types.VARCHAR);
            statement.setNull(2, Types.VARCHAR);
            statement.setString(3, gameName);
            statement.setString(4, json);
            statement.executeUpdate();

            try (var keys = statement.getGeneratedKeys()){
                if (keys.next()) {
                    int id = keys.getInt(1);
                    return new GameData (id, null, null, gameName, chessGame);
                }
            }
            throw new DataAccessException("Failed to retrieve game ID");
        } catch (SQLException e) {
            throw new DataAccessException("Error create game: " + e.getMessage());
        }
    }

    @Override
    public GameData getGame(int id) {
        return null;
    }

    @Override
    public Collection<GameData> listGames() {
        return List.of();
    }

    @Override
    public void updateGamePlayers(int id, String playerColor, String username) {

    }

    @Override
    public void clear() {

    }
}
