import dev.gump.worm.entity.Entity;
import dev.gump.worm.field.Field;
import dev.gump.worm.field.Relationship;

import java.util.UUID;

public class Item extends Entity {
    @Field(unique = true, autoGenerate = true)
    public int itemId;
    @Field
    public String name;
    @Field
    public int force;

    public Item() {
    }

    public Item(String name, int force) {
        this.name = name;
        this.force = force;
        this.insert();
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getForce() {
        return force;
    }

    public void setForce(int force) {
        this.force = force;
    }
}

