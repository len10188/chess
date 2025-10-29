package dataaccess;

import com.google.gson.Gson;
import model.AuthData;

import java.sql.*;
import java.util.*;

public class SQLAuthDAO implements AuthDAO {

    private final Gson gson = new Gson();

    private final String[] createStatement = {
            """
            CREATE TABLE IF NOT EXISTS authTokens (
            
"""
    };

    @Override
    public AuthData createAuth(String username) {
        return null;
    }

    @Override
    public AuthData getAuth(String token) {
        return null;
    }

    @Override
    public void deleteAuth(String token) {

    }

    @Override
    public void clear() {

    }
}
