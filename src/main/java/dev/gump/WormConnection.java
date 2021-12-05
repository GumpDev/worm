package dev.gump;

public class WormConnection {
    String ip = "", user = "", password = "", database = "";
    int port = 3306;

    boolean debug = false;

    public WormConnection(String ip, int port, String database, String user, String password) {
        this.ip = ip;
        this.user = user;
        this.password = password;
        this.database = database;
        this.port = port;
    }

    public WormConnection(String ip, int port, String database, String user) {
        this.ip = ip;
        this.user = user;
        this.database = database;
        this.port = port;
    }

    public WormConnection(String ip, String database, String user, String password) {
        this.ip = ip;
        this.user = user;
        this.password = password;
        this.database = database;
    }

    public WormConnection(String ip, String database, String user) {
        this.ip = ip;
        this.user = user;
        this.database = database;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getConnectionString(){
        return "jdbc:mysql://"+ ip +":"+ port +"/"+ database;
    }

    public String getIp() {
        return ip;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabase() {
        return database;
    }

    public int getPort() {
        return port;
    }
}
