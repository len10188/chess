package server;


import dataaccess.*;
import handler.UserHandler;
import handler.LoginHandler;
import handler.LogoutHandler;
import handler.ListGamesHandler;

import service.UserService;
import service.LoginService;
import service.LogoutService;
import service.ListGamesService;

import io.javalin.*;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.

        // Create DAO
        UserDAO userDAO = new MemoryUserDOA();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();

        // Create services
        UserService userService = new UserService(userDAO, authDAO);
        LoginService loginService = new LoginService(userDAO, authDAO);
        LogoutService logoutService = new LogoutService(authDAO);
        ListGamesService listGamesService = new ListGamesService(authDAO, gameDAO);


        // Create handlers
        UserHandler userHandler = new UserHandler(userService);
        LoginHandler loginHandler = new LoginHandler(loginService);
        LogoutHandler logoutHandler = new LogoutHandler(logoutService);
        ListGamesHandler listGamesHandler = new ListGamesHandler(listGamesService);

        // routes
        javalin.post("/user", userHandler.registerUser);
        javalin.post("/session", loginHandler.loginUser);
        javalin.delete("/session", logoutHandler.logoutUser);
        javalin.get("/games", listGamesHandler.listGames);

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
