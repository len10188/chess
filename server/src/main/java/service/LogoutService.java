package service;

import dataaccess.AuthDAO;
import model.AuthData;
import request.LogoutRequest;
import result.LogoutResult;

public class LogoutService {
    private final AuthDAO authDAO;

    public LogoutService(AuthDAO authDAO){
        this.authDAO = authDAO;
    }

    public LogoutResult logout(LogoutRequest request)
            throws ServiceException.UnauthorizedException, ServiceException.BadRequestException {

        // check authToken
        if (request.authToken() == null || request.authToken().isEmpty()){
            throw new ServiceException.BadRequestException();
        }

        // Validate token
        AuthData auth = authDAO.getAuth(request.authToken());
        if (auth == null) { // authToken empty
            throw new ServiceException.UnauthorizedException();
        }

        // Delete token
        authDAO.deleteAuth(request.authToken());

        //return empty result
        return new LogoutResult();
    }
}
