package dev.gump.worm.typeadapter;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DateTypeAdapter implements WormTypeAdapter<Date> {
    @Override
    public String toDatabase(Date input) {
        return input.toString();
    }

    @Override
    public Date fromDatabase(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getDate(columnName);
    }
}
