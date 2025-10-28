package dataaccess;

import javax.xml.crypto.Data;

public class DAOSwitch {
    private static final boolean USE_DATABASE = true;

    public static GameDAO getGameDAO() throws DataAccessException {
        if (USE_DATABASE) {
            return new SQLGameDAO();
        } else {
            return new MemoryGameDAO();
        }
    }
}
