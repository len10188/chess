package service;

import dataaccess.DAOSwitch;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDOA;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import request.LoginRequest;
import result.LoginResult;

import static org.junit.jupiter.api.Assertions.*;

public class LoginServiceTests {
    private LoginService loginService;

    @BeforeEach
    void setUp() {
        DAOSwitch.useMemory();
        MemoryUserDOA userDOA = new MemoryUserDOA();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        loginService = new LoginService(userDOA, authDAO);

        // load a known user
        userDOA.createUser(new UserData("ExistingUser", "password", "email"));

    }

    @Test
    void loginSuccessful() throws Exception{
        LoginRequest request = new LoginRequest("ExistingUser", "password");

        LoginResult result = loginService.login(request);

        // Assert
        assertNotNull(result);
        assertEquals("ExistingUser", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    void loginIncorrectPassword() {
        LoginRequest request = new LoginRequest("ExistingUser", "wrongPass");

        assertThrows(ServiceException.UnauthorizedException.class, () -> loginService.login(request));
    }
}
