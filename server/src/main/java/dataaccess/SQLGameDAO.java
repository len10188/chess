package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

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
    public GameData getGame(int id) throws DataAccessException {
        String sql = "SELECT * FROM games WHERE gameID = ?";
        try (Connection conn = DatabaseManager.getConnection();
             var statement =  conn.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String whiteU = resultSet.getString("whiteUsername");
                    String blackU = resultSet.getString("blackUsername");
                    String gameName = resultSet.getString("gameName");
                    var json = resultSet.getString("game");
                    ChessGame chessGame = gson.fromJson(json, ChessGame.class);
                    return new GameData(id, whiteU, blackU, gameName, chessGame);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving game: " + e.getMessage());
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        ArrayList<GameData> games = new ArrayList<>();
        String sql = "SELECT * FROM games";
        try (Connection conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(sql);
             var resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("gameID");
                String whiteU = resultSet.getString("whiteUsername");
                String blackU = resultSet.getString("blackUsername");
                String gameName = resultSet.getString("gameName");
                var json = resultSet.getString("game");
                ChessGame chessGame = gson.fromJson(json, ChessGame.class);
                games.add(new GameData(id, whiteU, blackU, gameName, chessGame));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing games: " + e.getMessage());
        }
        return games;
    }

    @Override
    public void updateGamePlayers(int id, String playerColor, String username) throws DataAccessException {
        var column = switch (playerColor.toLowerCase()) {
            case "white" -> "whiteUsername";
            case "black" -> "blackUsername";
            default -> throw new DataAccessException("Invalid player color: " + playerColor);
        };

        var checkColorSql = "SELECT " + column + " FROM games WHERE gameID = ?";
        try (Connection conn = DatabaseManager.getConnection();
            var checkStatement = conn.prepareStatement(checkColorSql)) {

            checkStatement.setInt(1, id);
            try (var resultSet = checkStatement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new DataAccessException("Game not found");
                }
                var existingPlayer = resultSet.getString(column);
                if (existingPlayer != null) {
                    throw new DataAccessException("Player color already taken");
                }
            }

            var updateSql = "UPDATE games SET " + column + " = ? WHERE gameID = ?";
            try (var updateStatement = conn.prepareStatement(updateSql)) {
                updateStatement.setString(1, username);
                updateStatement.setInt(2, id);
                updateStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating player: " + e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
        var statement = conn.prepareStatement("TRUNCATE TABLE games")) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing games: " + e.getMessage());
        }
    }
}
