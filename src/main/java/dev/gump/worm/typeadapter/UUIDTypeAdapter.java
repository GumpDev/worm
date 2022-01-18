package dev.gump.worm.typeadapter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UUIDTypeAdapter implements WormTypeAdapter<UUID> {
    @Override
    public String toDatabase(UUID input) {
        return input.toString();
    }

    @Override
    public UUID fromDatabase(ResultSet resultSet, String columnName) throws SQLException {
        return UUID.fromString(resultSet.getString(columnName));
    }
}
