package dev.gump.worm.typeadapter;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BigDecimalTypeAdapter implements WormTypeAdapter<BigDecimal> {
    @Override
    public String toDatabase(BigDecimal input) {
        return input.toString();
    }

    @Override
    public BigDecimal fromDatabase(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getBigDecimal(columnName);
    }
}
