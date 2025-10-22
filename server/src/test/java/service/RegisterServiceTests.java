package service;

import dataaccess.AuthDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDOA;
import dataaccess.UserDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import request.RegisterRequest;
import result.RegisterResult;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterServiceTests {
    private UserService userService;

    @BeforeEach
    void setUp() {
        UserDAO userDAO = new MemoryUserDOA();
        AuthDAO authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);
    }

    @Test
    void registerSuccessful() throws Exception {
        RegisterRequest request = new RegisterRequest("newUser", "password", "email@test.com");

        RegisterResult result = userService.register(request);

        // Assert
        assertNotNull(result); // check if result was returned
        assertEquals("newUser", result.username()); // check username matches
        assertNotNull(result.authToken()); // check authToken was created
    }

    @Test
    void registerUsernameTaken() throws Exception {
        RegisterRequest first = new RegisterRequest(
                "duplicateUser",
                "password" ,
                "email@test.com");

        RegisterRequest second = new RegisterRequest(
                "duplicateUser",
                "password" ,
                "OtherEmail@test.com");

        // register first one
        userService.register(first);

        // attempt register second it should throw error
        assertThrows(ServiceException.AlreadyTakenException.class, () -> userService.register(second));

    }
}
