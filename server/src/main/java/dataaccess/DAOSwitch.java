package dataaccess;

public class DAOSwitch {
    private static boolean useDatabase = true;// false = memory, true = database.

    public static void useMemory() {
        useDatabase = false;}
    public static void useDatabase() {
        useDatabase = true;}
    public static GameDAO setGameDAO() throws DataAccessException {
        if (useDatabase) {
            return new SQLGameDAO();
        } else {
            return new MemoryGameDAO();
        }
    }
    public static AuthDAO setAuthDAO() throws DataAccessException {
        if (useDatabase) {
            return new SQLAuthDAO();
        } else {
            return new MemoryAuthDAO();
        }
    }
    public static UserDAO setUserDAO() throws DataAccessException {
        if (useDatabase) {
            return new SQLUserDAO();
        } else {
            return new MemoryUserDOA();
        }
    }
}
