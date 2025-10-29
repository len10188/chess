package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import request.ListGamesRequest;
import result.ListGamesResult;

import java.util.Collection;

public class ListGamesService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public ListGamesService(AuthDAO authDAO, GameDAO gameDAO){
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public ListGamesResult listGames(ListGamesRequest request)
            throws ServiceException.UnauthorizedException, DataAccessException {

        // validate token
        AuthData auth = authDAO.getAuth(request.authToken());
        if (auth == null) {
            throw new ServiceException.UnauthorizedException();
        }

        // get all games
        Collection<GameData> allGames = gameDAO.listGames();
        return new ListGamesResult(allGames);
    }
}
