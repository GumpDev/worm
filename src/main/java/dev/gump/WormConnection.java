package dev.gump;

public class WormConnection {
    private String ip = "", user = "", password = "", database = "";
    private int port = 3306;
    private boolean debug = false;

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

    public String getUrl(){
        return "jdbc:mysql://"+ ip +":"+ port +"/"+ database;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

}
