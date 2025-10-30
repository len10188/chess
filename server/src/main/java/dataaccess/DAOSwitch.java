package dataaccess;

public class DAOSwitch {
    private static boolean use_datebase = true;// false = memory, true = database.

    public static void useMemory() {
        use_datebase = false;}
    public static void useDatabase() {
        use_datebase = true;}
    public static GameDAO setGameDAO() throws DataAccessException {
        if (use_datebase) {
            return new SQLGameDAO();
        } else {
            return new MemoryGameDAO();
        }
    }
    public static AuthDAO setAuthDAO() throws DataAccessException {
        if (use_datebase) {
            return new SQLAuthDAO();
        } else {
            return new MemoryAuthDAO();
        }
    }
    public static UserDAO setUserDAO() throws DataAccessException {
        if (use_datebase) {
            return new SQLUserDAO();
        } else {
            return new MemoryUserDOA();
        }
    }
}
