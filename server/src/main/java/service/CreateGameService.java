package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import result.CreateGameResult;

import java.sql.SQLException;

public class CreateGameService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public CreateGameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public CreateGameResult createGame(String authToken, String gameName)
            throws ServiceException.UnauthorizedException, ServiceException.BadRequestException, SQLException, DataAccessException {

        // authToken is bad.
        if (authToken == null || authToken.isEmpty()) {
            throw new ServiceException.UnauthorizedException();
        }

        // game name is bad
        if (gameName == null || gameName.isEmpty()) {
            throw new ServiceException.BadRequestException();
        }

        // check authToken
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new ServiceException.UnauthorizedException();
        }

        // create game
        GameData newGame = gameDAO.createGame(gameName);

        // return result
        return new CreateGameResult(newGame.gameID());
    };
}
