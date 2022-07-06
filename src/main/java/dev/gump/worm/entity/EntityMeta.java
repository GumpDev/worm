package dev.gump.worm.entity;

import dev.gump.worm.WormException;
import dev.gump.worm.field.Context;
import dev.gump.worm.field.SequenceType;
import dev.gump.worm.field.WormContext;
import dev.gump.worm.field.WormField;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EntityMeta {
    private final String name;
    private final Class<? extends Entity> entityClass;
    private final Collection<WormField> commonColumns, uniqueKeyColumns, allColumns;

    private final List<WormContext> contexts = new ArrayList<>();
    @Nullable
    private WormField autoIncrementColumn;

    public EntityMeta(Class<? extends Entity> entityClass, String name, Collection<WormField> fields, List<Context> contexts) {
        this.entityClass = entityClass;
        this.name = name;
        Collection<WormField> commonColumns = new ArrayList<>();
        Collection<WormField> uniqueKeyColumns = new ArrayList<>();
        Collection<WormField> allColumns = new ArrayList<>();

        for(WormField field : fields){
            if(field.isUnique()) {
                if(field.getIncrement() == SequenceType.AUTO_INCREMENT)
                    autoIncrementColumn = field;
                uniqueKeyColumns.add(field);
            }else
                commonColumns.add(field);
            allColumns.add(field);
        }

        for(Context context : contexts){
            List<String> fieldsContexted = allColumns.stream().map(WormField::getName).collect(Collectors.toList());
            for(String field : context.ignoredFields())
                fieldsContexted.remove(field);
            this.contexts.add(new WormContext(context, fieldsContexted));
            System.out.println(context.name());
        }

        this.commonColumns = commonColumns;
        this.uniqueKeyColumns = uniqueKeyColumns;
        this.allColumns = allColumns;
    }

    public String getName() {
        return name;
    }

    public Class<? extends Entity> getEntityClass() {
        return entityClass;
    }

    public WormField getColumn(String name){
        for(WormField field : allColumns){
            if(field.getName().equals(name))
                return field;
        }
        return null;
    }

    public Collection<WormField> getCommonColumns() {
        return commonColumns;
    }

    public Collection<WormField> getUniqueKeyColumns() {
        return uniqueKeyColumns;
    }

    public Collection<WormField> getColumns() {
        return allColumns;
    }

    public @Nullable WormField getAutoIncrementColumn() {
        return autoIncrementColumn;
    }

    public String getFieldsFromContext(String context){
        for(WormContext context1 : contexts){
            if(Objects.equals(context1.getName(), context)) return context1.getFields();
        }
        throw new WormException("Context " + context + " not found!");
    }
}
