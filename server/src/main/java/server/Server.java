package server;


import dataaccess.*;
import handler.*;

import service.*;

import io.javalin.*;



public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        UserDAO userDAO;
        AuthDAO authDAO;
        GameDAO gameDAO;
        try {
            // Set DAO type
            DAOSwitch.useDatabase();

            // Create DAO
            userDAO = DAOSwitch.setUserDAO();
            authDAO = DAOSwitch.setAuthDAO();
            gameDAO = DAOSwitch.setGameDAO();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        // Create services
        UserService userService = new UserService(userDAO, authDAO);
        LoginService loginService = new LoginService(userDAO, authDAO);
        LogoutService logoutService = new LogoutService(authDAO);
        ListGamesService listGamesService = new ListGamesService(authDAO, gameDAO);
        CreateGameService createGameService = new CreateGameService(authDAO, gameDAO);
        JoinGameService joinGameService = new JoinGameService(authDAO, gameDAO);
        ClearService clearService = new ClearService(userDAO, gameDAO, authDAO);


        // Create handlers
        UserHandler userHandler = new UserHandler(userService);
        LoginHandler loginHandler = new LoginHandler(loginService);
        LogoutHandler logoutHandler = new LogoutHandler(logoutService);
        ListGamesHandler listGamesHandler = new ListGamesHandler(listGamesService);
        CreateGameHandler createGameHandler = new CreateGameHandler(createGameService);
        JoinGameHandler joinGameHandler = new JoinGameHandler(joinGameService);
        ClearHandler clearHandler = new ClearHandler(clearService);

        // routes
        javalin.post("/user", userHandler.registerUser);
        javalin.post("/session", loginHandler.loginUser);
        javalin.delete("/session", logoutHandler.logoutUser);
        javalin.get("/game", listGamesHandler.listGames);
        javalin.post("/game", createGameHandler.createGame);
        javalin.put("/game", joinGameHandler.joinGame);
        javalin.delete("/db", clearHandler.clearAll);

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
