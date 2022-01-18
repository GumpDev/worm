package dev.gump.worm;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class WormTableMeta {

    private final String name;
    private final Collection<WormColumn> commonColumns, primaryKeyColumns, allColumns;

    @Nullable
    private final WormColumn autoIncrementColumn;

    public WormTableMeta(String name, Collection<WormColumn> commonColumns, Collection<WormColumn> primaryKeyColumns, @Nullable WormColumn autoIncrementColumn) {
        this.name = name;
        this.commonColumns = commonColumns;
        this.primaryKeyColumns = primaryKeyColumns;
        this.autoIncrementColumn = autoIncrementColumn;

        List<WormColumn> allColumns = new ArrayList<>();
        allColumns.addAll(this.primaryKeyColumns);
        allColumns.addAll(this.commonColumns);
        if (this.autoIncrementColumn != null)
            allColumns.add(this.autoIncrementColumn);
        this.allColumns = allColumns;
    }

    public String getName() {
        return name;
    }

    public Collection<WormColumn> getCommonColumns() {
        return commonColumns;
    }

    public Collection<WormColumn> getPrimaryKeyColumns() {
        return primaryKeyColumns;
    }

    @Nullable
    public WormColumn getAutoIncrementColumn() {
        return autoIncrementColumn;
    }

    public Collection<WormColumn> getAllColumns() {
        return allColumns;
    }
}
