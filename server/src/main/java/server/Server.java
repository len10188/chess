package server;

import handler.LoginHandler;
import handler.LogoutHandler;
import io.javalin.*;
import handler.UserHandler;
import org.eclipse.jetty.server.Authentication;
import service.LoginService;
import service.UserService;
import service.LogoutService;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDOA;
import dataaccess.UserDAO;
import dataaccess.AuthDAO;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.

        // Create DAO
        UserDAO userDAO = new MemoryUserDOA();
        AuthDAO authDAO = new MemoryAuthDAO();

        // Create services
        UserService userService = new UserService(userDAO, authDAO);
        LoginService loginService = new LoginService(userDAO, authDAO);
        LogoutService logoutService = new LogoutService(authDAO);

        // Create handlers
        UserHandler userHandler = new UserHandler(userService);
        LoginHandler loginHandler = new LoginHandler(loginService);
        LogoutHandler logoutHandler = new LogoutHandler(logoutService);

        // routes
        javalin.post("/user", userHandler.registerUser);
        javalin.post("/session", loginHandler.loginUser);
        javalin.delete("/session", logoutHandler.logoutUser);


    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
