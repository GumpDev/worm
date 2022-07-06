package dev.gump.worm.typeadapter;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BooleanTypeAdapter implements WormTypeAdapter<Boolean> {
    @Override
    public String toDatabase(Boolean input) {
        return !input ? "0" : "1";
    }

    // Will not be used
    @Override
    public Boolean fromDatabase(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getBoolean(columnName);
    }
}
