package dev.gump.worm;

public class WormConnection {
    private String ip = "", user = "", password = "", database = "";
    private int port = 3306, maxPoolSize = 10, minPoolSize = 3;
    private long maxLifetime = 30000, leakDetectionThreshold = 5000;
    private boolean debug = false;

    public WormConnection(String ip, int port, String user, String password, String database) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.password = password;
        this.database = database;
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

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public void setMinPoolSize(int minPoolSize) {
        this.minPoolSize = minPoolSize;
    }

    public long getMaxLifetime() {
        return maxLifetime;
    }

    public void setMaxLifetime(long maxLifetime) {
        this.maxLifetime = maxLifetime;
    }

    public long getLeakDetectionThreshold() {
        return leakDetectionThreshold;
    }

    public void setLeakDetectionThreshold(long leakDetectionThreshold) {
        this.leakDetectionThreshold = leakDetectionThreshold;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getUrl(){
        return "jdbc:mysql://" + ip + ":" + port + "/" + database;
    }

}
