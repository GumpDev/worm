import dev.gump.worm.entity.Entity;
import dev.gump.worm.field.Context;
import dev.gump.worm.field.Field;

import java.util.UUID;

public class UserRelation extends Entity {

    @Field(unique = true, autoGenerate = true)
    public UUID relationId;
    @Field
    public String text;
    @Field
    public User user;

    public UserRelation() {
    }

    public UserRelation(String text, User user) {
        this.text = text;
        this.user = user;
        this.insert();
    }


    public UUID getRelationId() {
        return relationId;
    }

    public String getText() {
        return text;
    }

    public User getUser() {
        return user;
    }
}
