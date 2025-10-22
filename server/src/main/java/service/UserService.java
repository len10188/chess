package service;
import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import request.RegisterRequest;
import result.RegisterResult;

public class UserService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO){
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(RegisterRequest request)
            throws ServiceException.BadRequestException, ServiceException.AlreadyTakenException
    {
        // check if request is missing information.
        if ((request.username() == null) || (request.password() == null) || (request.email() == null)){
            throw new ServiceException.BadRequestException();
        }

        // check if user already exists
        UserData existing = userDAO.getUser(request.username());
        if (existing != null){
            throw new ServiceException.AlreadyTakenException();
        }

        // create new user
        UserData newUser = new UserData(request.username(), request.password(), request.email());
        userDAO.createUser(newUser);

        // create authToken
        AuthData auth = authDAO.createAuth(request.username());

        //return result to handler
        return new RegisterResult(request.username(), auth.authToken());
    }

}
