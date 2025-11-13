package client;

import org.junit.jupiter.api.*;
import server.Server;
import ui.ServerFacade;
import model.GameData;
import ui.ServiceException;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private ServerFacade facade;
    private static String baseUrl;


    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        baseUrl = "http://localhost:" + port;
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void setup() throws Exception {
        facade = new ServerFacade(baseUrl);
        facade.clear();
    }

    // REGISTER
    @Test
    public void registerPositive() throws Exception {
        String token = facade.register("laura", "password", "email@byu.edu");
        assertNotNull(token, "register should return a token");
    }

    @Test
    public void registerNegative() throws Exception {
        facade.register("laura", "password", "email@byu.edu");
        assertThrows(ServiceException.AlreadyTakenException.class, () -> {
                    facade.register("laura", "password", "email2@byu.edu");
                });
    }

    // LOGIN
    @Test
    public void loginPositive() throws Exception {
        facade.register("loginTest", "password", "email@byu.edu");
        String token = facade.login("loginTest", "password");
        assertNotNull(token, "login should return a token upon success.");
    }

    @Test
    public void loginNegative() throws Exception {
        facade.register("loginTest", "password", "email@byu.edu");
        String token = facade.login("loginTest", "wrongPass");
        assertNull(token, "login should return null if login fail.");
    }

    // LOGOUT
    @Test
    public void logoutPositive() throws Exception {
        facade.register("logoutTest", "password", "email@byu.edu");
        assertDoesNotThrow(facade::logout, "logout after login or register should not throw");
    }

    @Test
    public void logoutNegativeNotLoggedIn() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> facade.logout(),
                "logout without login should throw");
        assertTrue(ex.getMessage().toLowerCase().contains("not logged in"));
    }

    // CREATE GAME
    @Test
    public void createGamePositive() throws Exception {
        facade.register("createGame", "password", "test@email.com");
        String gameName = facade.createGame("game1");
        assertEquals("game1", gameName, "create game should return gameData not null upon success");
    }

    @Test
    public void createGameNegative() throws Exception {
        String gameName = facade.createGame("notLoggedIn");
        assertNull(gameName, "create game while not logged in should return null");
    }

    // LIST GAME
    @Test
    void listGamesPositive() throws Exception {
        facade.register("listGames", "password", "test@email.com");
        facade.createGame("A");
        facade.createGame("B");
        Collection<GameData> games = facade.listGames();
        assertNotNull(games, "listGames should not be null");
        assertTrue(games.size() >= 2, "should list at least the two created games");
    }

    @Test
    void listGamesNegativeNotLoggedIn() throws Exception {
        Collection<GameData> games = facade.listGames();
        // Your facade returns null if response was null / error
        assertNull(games, "listGames when not logged in should return null");
    }


    //JOIN GAME
    @Test
    void joinGamePositive() throws Exception {
        String user1 = "player1";
        facade.register(user1, "password", "test@email.com");

        String createdName = facade.createGame("TESTGAME");
        assertNotNull(createdName, "game should be made");

        // find the gameID by name
        var games = facade.listGames();
        assertNotNull(games, "listGames should not be null after login");
        var game = games.stream()
                .filter(g -> "TESTGAME".equals(g.gameName()))
                .findFirst().orElseThrow();

        // join as white
        assertTrue(facade.joinGame("white", game.gameID()),
                "joining an existing game as white should succeed");

        // verify state
        var after = facade.listGames();
        var found = after.stream().filter(g -> g.gameID() == game.gameID()).findFirst().orElse(null);
        assertNotNull(found, "created game should still exist");
        assertEquals(user1, found.whiteUsername(), "white seat should be the creator");
    }

    @Test
    void joinGameNegativeAlreadyTaken() throws Exception {
        // user1 creates game and takes white
        ServerFacade f1 = new ServerFacade(baseUrl);
        f1.register("player1", "password", "p1@email.com");

        String created = f1.createGame("TestGame");
        assertNotNull(created, "game should be created");

        GameData game = f1.listGames().stream()
                .filter(g -> "TestGame".equals(g.gameName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Created game not found in listGames"));

        assertTrue(f1.joinGame("white", game.gameID()),
                "first player should be able to claim white");

        // user2 tries to also take white (should be rejected)
        ServerFacade f2 = new ServerFacade(baseUrl);
        f2.register("player2", "password", "p2@email.com");

        boolean joined = f2.joinGame("white", game.gameID());
        assertFalse(joined, "second player should be rejected when seat already taken");

        // verify white seat still belongs to player1
        Collection<GameData> games = f2.listGames();
        assertNotNull(games, "listGames should not be null");
        GameData found = games.stream()
                .filter(g -> g.gameID() == game.gameID())
                .findFirst()
                .orElse(null);

        assertNotNull(found, "game should still exist");
        assertEquals("player1", found.whiteUsername(),
                "white seat should remain with the first player after second attempt");
    }
    // CLEAR
    @Test
    void clearPositiveWipesData() throws Exception {
        ServerFacade f = new ServerFacade(baseUrl);

        // Create data
        String t1 = f.register("test1", "pw", "test@email.com");
        String g = f.createGame("G1");

        // clear the DB
        assertDoesNotThrow(f::clear, "clear() should not throw");

        // Old token should now be invalid -> any authed call should fail (return null)
        var gamesAfter = f.listGames();
        assertNull(gamesAfter, "old auth token should be invalid after clear()");

        // You can register the same username again because DB is wiped
        f.authToken = null; // ensure no stale header on /user
        String t2 = f.register("test1", "pw", "test@email.com");
        assertNotNull(t2, "re-registering same username should succeed after clear()");
    }

    @Test
    void clearNegativePreviousUserCannotLoginAfterClear() throws Exception {
        ServerFacade f = new ServerFacade(baseUrl);

        // Create data
        assertNotNull(f.register("test1", "pw", "b@email.com"));
        assertNotNull(f.createGame("G2"));

        // Clear DB
        assertDoesNotThrow(f::clear);

        // Now the old user should no longer exist -> login should fail
        f.authToken = null; // login should not send any (now-invalid) header
        String token = f.login("test1", "pw");
        assertNull(token, "login for a user created before clear() should fail");
    }

}

