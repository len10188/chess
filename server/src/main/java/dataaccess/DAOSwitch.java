package dataaccess;

public class DAOSwitch {
    private static boolean USE_DATABASE = true;// false = memory, true = database.

    public static void useMemory() {USE_DATABASE = false;}
    public static void useDatabase() {USE_DATABASE = true;}
    public static GameDAO setGameDAO() throws DataAccessException {
        if (USE_DATABASE) {
            return new SQLGameDAO();
        } else {
            return new MemoryGameDAO();
        }
    }
    public static AuthDAO setAuthDAO() throws DataAccessException {
        if (USE_DATABASE) {
            return new SQLAuthDAO();
        } else {
            return new MemoryAuthDAO();
        }
    }
    public static UserDAO setUserDAO() throws DataAccessException {
        if (USE_DATABASE) {
            return new SQLUserDAO();
        } else {
            return new MemoryUserDOA();
        }
    }
}
