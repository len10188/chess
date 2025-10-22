package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
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

    public LoginResult login(LoginRequest request) throws ServiceException.UnauthorizedException, ServiceException.BadRequestException {
        // Check if valid request
        if (request.username() == null || request.password() == null) {
            throw new ServiceException.BadRequestException();
        }

        // get user from database
        UserData user = userDAO.getUser(request.username());
        if (user == null){
            throw new ServiceException.UnauthorizedException();
        }

        // check password
        if (!user.password().equals(request.password())){
            throw new ServiceException.UnauthorizedException();
        }

        //create AuthToken
        AuthData userAuth = authDAO.createAuth(request.username());

        return new LoginResult(request.username(), userAuth.authToken());
    }
}
