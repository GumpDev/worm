import dev.gump.worm.entity.Entity;
import dev.gump.worm.field.Context;
import dev.gump.worm.field.Field;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Context(name = "withoutAge", ignoredFields = {"age"})
public class User extends Entity {

    @Field(unique = true, autoGenerate = true)
    public UUID userId;
    @Field
    public String name;
    @Field
    public int age;

    public User() {

    }

    public User(String name, int age) throws ExecutionException, InterruptedException {
        this.name = name;
        this.age = age;
        this.insert().get();
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
