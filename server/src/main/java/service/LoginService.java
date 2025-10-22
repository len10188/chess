package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import io.javalin.http.UnauthorizedResponse;
import jakarta.servlet.UnavailableException;
import model.AuthData;
import model.UserData;
import request.LoginRequest;
import result.LoginResult;

public class LoginService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public LoginService(UserDAO userDAO, AuthDAO authDAO){
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public LoginResult login(LoginRequest request) throws serviceException.UnauthorizedException, serviceException.BadRequestException {
        // Check if valid request
        if (request.username() == null || request.password() == null) {
            throw new serviceException.BadRequestException();
        }

        // get user from database
        UserData user = userDAO.getUser(request.username());
        if (user == null){
            throw new serviceException.UnauthorizedException();
        }

        // check password
        if (!user.password().equals(request.password())){
            throw new serviceException.UnauthorizedException();
        }

        //create AuthToken
        AuthData userAuth = authDAO.createAuth(request.username());

        return new LoginResult(request.username(), userAuth.authToken());
    }
}
