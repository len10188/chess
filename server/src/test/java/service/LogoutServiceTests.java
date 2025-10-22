package service;

import dataaccess.MemoryAuthDAO;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import request.LogoutRequest;
import result.LogoutResult;

import static org.junit.jupiter.api.Assertions.*;

public class LogoutServiceTests {
    private MemoryAuthDAO authDAO;
    private LogoutService logoutService;

    @BeforeEach
    void setUp() {
        authDAO = new MemoryAuthDAO();
        logoutService = new LogoutService(authDAO);

        // load token
        authDAO.createAuth("ExistingUser");
    }

    @Test
    void logoutSuccessful() throws Exception {

        AuthData auth = authDAO.createAuth("ExistingUser");
        String token = auth.authToken();

        LogoutRequest request = new LogoutRequest(token);

        LogoutResult result = logoutService.logout(request);

        // Assert
        assertNotNull(result);
        assertNull(authDAO.getAuth(auth.authToken()), "Auth token should be removed after logout");
    }

    @Test
    void loginIncorrectPassword() {
        LogoutRequest request = new LogoutRequest("invalidToken");

        // assert
        assertThrows(ServiceException.UnauthorizedException.class, () -> logoutService.logout(request));
    }
}

