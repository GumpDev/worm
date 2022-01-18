package dev.gump.worm.typeadapter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class TimestampTypeAdapter implements WormTypeAdapter<Timestamp> {
    @Override
    public String toDatabase(Timestamp input) {
        return input.toString();
    }

    @Override
    public Timestamp fromDatabase(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getTimestamp(columnName);
    }
}
