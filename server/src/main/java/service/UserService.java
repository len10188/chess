package service;
import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import org.eclipse.jetty.server.Authentication;
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
    {
        // check if request is missing information.
        if ((request.username() == null) || (request.password() == null) || (request.email() == null)){
            throw new BadRequestException();
        }

        // check if user already exists
        UserData existing = userDAO.getUser(request.username());
        if (existing != null){
            throw new AlreadyTakenException();
        }

        // create new user
        UserData newUser = new UserData(request.username(), request.password(), request.email());
        userDAO.createUser(newUser);

        // create authToken
        AuthData auth = authDAO.createAuth(request.username());

        //return result to handler
        return new RegisterResult(request.username(), auth.authToken());
    }
    public static class AlreadyTakenException extends Exception{}
    public static class BadRequestException extends Exception{}
}
