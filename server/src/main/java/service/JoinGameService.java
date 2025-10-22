package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import request.JoinGameRequest;
import result.JoinGameResult;

public class JoinGameService {

    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public JoinGameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public JoinGameResult joinGame(JoinGameRequest request)
        throws ServiceException.UnauthorizedException,
            ServiceException.AlreadyTakenException,
            ServiceException.BadRequestException
    {
        // validate input
        if (request.authToken() == null || request.authToken().isEmpty()) {
            throw new ServiceException.UnauthorizedException();
        }
        if (request.playerColor() == null || request.gameID() == 0) {
            throw new ServiceException.BadRequestException();
        }

        // Authenticate user
        AuthData auth = authDAO.getAuth(request.authToken());
        if (auth == null) {
            throw new ServiceException.UnauthorizedException();
        }

        // Fetch game
        GameData game = gameDAO.getGame(request.gameID());
        if (game == null) {
            throw new ServiceException.BadRequestException(); // game does not exist
        }

        // Check if team color is available
        String username = auth.username();
        switch (request.playerColor().toLowerCase()){ // make all colors lowercase
            case "white" -> {
                if (game.whiteUsername() != null ) throw new ServiceException.AlreadyTakenException();
                gameDAO.updateGamePlayers(request.gameID(), "white", username);
            }
            case "black" -> {
                if (game.blackUsername() != null) throw new ServiceException.AlreadyTakenException();
                gameDAO.updateGamePlayers(request.gameID(), "black", username);
            }
            default -> throw new ServiceException.BadRequestException();
        }
        return new JoinGameResult();
    }

}
