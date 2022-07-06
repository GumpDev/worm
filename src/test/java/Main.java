import dev.gump.worm.Worm;
import dev.gump.worm.WormConnection;
import dev.gump.worm.builders.Op;

import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Worm.getRegistry().registerTable(Item.class);
        Worm.getRegistry().registerTable(User.class);
        Worm.getRegistry().registerTable(UserRelation.class);

        WormConnection connection = new WormConnection("localhost", 3307, "root", "teste123", "rabbithole");
        connection.setDebug(true);

        Worm.initialize(connection);

        //User user = new User("Gump", 10);
        User user = (User) Worm.of(User.class).where(Op.equals("userId", "01417970-44fc-4672-b5a0-4a17bfc72585")).context("withoutAge").findOne().get();
        //UserRelation userRelation = new UserRelation("teste", user);
        UserRelation userRelation = (UserRelation) Worm.of(UserRelation.class).findUnique("8084bf17-54b1-4641-ad7f-09b53bfbd1c1").get();
        System.out.println(userRelation.user.name);
    }
}
