package dev.gump.worm.field;

import dev.gump.worm.entity.Entity;
import dev.gump.worm.entity.EntityMeta;

import java.util.List;

public class WormContext {
    private final String name;
    private final List<String> fields;

    public WormContext(Context context, List<String> fields) {
        this.name = context.name();
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public List<String> getFieldsList() {
        return fields;
    }

    public String getFields(){
        return String.join(", ", fields);
    }
}
