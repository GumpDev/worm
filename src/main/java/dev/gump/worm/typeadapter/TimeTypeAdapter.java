package dev.gump.worm.typeadapter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;

public class TimeTypeAdapter implements WormTypeAdapter<Time> {
    @Override
    public String toDatabase(Time input) {
        return input.toString();
    }

    @Override
    public Time fromDatabase(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getTime(columnName);
    }
}
