package dataaccess;

public class DAOSwitch {
    private static boolean useDatebase = true;// false = memory, true = database.

    public static void useMemory() {
        useDatebase = false;}
    public static void useDatabase() {
        useDatebase = true;}
    public static GameDAO setGameDAO() throws DataAccessException {
        if (useDatebase) {
            return new SQLGameDAO();
        } else {
            return new MemoryGameDAO();
        }
    }
    public static AuthDAO setAuthDAO() throws DataAccessException {
        if (useDatebase) {
            return new SQLAuthDAO();
        } else {
            return new MemoryAuthDAO();
        }
    }
    public static UserDAO setUserDAO() throws DataAccessException {
        if (useDatebase) {
            return new SQLUserDAO();
        } else {
            return new MemoryUserDOA();
        }
    }
}
