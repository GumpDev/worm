package dev.gump.worm.typeadapter;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface WormTypeAdapter<T> {

    String toDatabase(T input);

    T fromDatabase(ResultSet resultSet, String columnName) throws SQLException;

}
