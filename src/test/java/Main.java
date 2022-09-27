import dev.gump.worm.Worm;
import dev.gump.worm.WormConnection;
import dev.gump.worm.builders.Op;

import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Worm.getRegistry().registerTable(Item.class);

        WormConnection connection = new WormConnection("localhost", 3306, "root", "teste123", "rabbithole");
        connection.setDebug(true);

        Worm.initialize(connection);

        Item item = new Item("teste", 2);
        item.insert();
    }
}
