package dataaccess;

import model.GameData;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;


public class SQLGameDAOTest {
    private SQLGameDAO gameDAO;

    @BeforeEach
    void setup() throws DataAccessException {
        DAOSwitch.useDatabase();
        gameDAO = new SQLGameDAO();
        gameDAO.clear();
    }

    @Test
    void createGamePositive() throws DataAccessException, SQLException {
        GameData game = gameDAO.createGame("test");
        assertNotNull(game);
        assertEquals("test", game.gameName());
    }

    @Test
    void createGameNegativeNullName() throws SQLException, DataAccessException {
        GameData game = gameDAO.createGame("");
        assertEquals("", game.gameName());
    }

    @Test
    void getGamePositive() throws DataAccessException, SQLException {
        GameData game = gameDAO.createGame("test");
        GameData testGet = gameDAO.getGame(game.gameID());

        assertEquals(game.gameID(), testGet.gameID());
        assertEquals(game.gameName(), testGet.gameName());
        assertEquals(game.whiteUsername(), testGet.whiteUsername());
        assertEquals(game.blackUsername(), testGet.blackUsername());
    }

    @Test
    void getGameNegative() throws DataAccessException {
        assertNull(gameDAO.getGame(1234));
    }

    @Test
    void listGamesPositive() throws DataAccessException, SQLException {
        gameDAO.createGame("game1");
        gameDAO.createGame("game2");

        Collection<GameData> gameList = gameDAO.listGames();

        assertEquals(2, gameList.size());
    }

    @Test
    void listGamesNegative() throws DataAccessException {
        Collection<GameData> gameList = gameDAO.listGames();

        assertTrue(gameList.isEmpty());
    }

    @Test
    void updateGamePlayersPositive() throws DataAccessException, SQLException {
        GameData game = gameDAO.createGame("test");
        gameDAO.updateGamePlayers(game.gameID(), "white", "player1");
        GameData updated = gameDAO.getGame(game.gameID());

        assertEquals("player1", updated.whiteUsername());
    }

    @Test
    void updateGamePlayersNegative() throws DataAccessException, SQLException {
        GameData game = gameDAO.createGame("test");

        assertThrows(DataAccessException.class,
                () -> gameDAO.updateGamePlayers(game.gameID(), "purple", "player1"));
    }

    @Test
    void clearPositive() throws DataAccessException, SQLException {
        GameData game = gameDAO.createGame("test");
        gameDAO.clear();

        assertNull(gameDAO.getGame(game.gameID()));

    }
}
