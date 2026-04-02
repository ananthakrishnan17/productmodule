package repository;

import java.sql.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class DBConnectionPool {
    private static final Logger LOG = Logger.getLogger(DBConnectionPool.class.getName());
    private static DBConnectionPool instance;

    private final BlockingQueue<Connection> pool;
    private final String url;
    private final String user;
    private final String password;

    private DBConnectionPool(String url, String user, String password, int poolSize) throws SQLException {
        this.url      = url;
        this.user     = user;
        this.password = password;
        this.pool     = new ArrayBlockingQueue<>(poolSize);
        for (int i = 0; i < poolSize; i++) {
            pool.offer(createConnection());
        }
    }

    public static synchronized DBConnectionPool getInstance() {
        if (instance == null) throw new IllegalStateException("Pool not initialised.");
        return instance;
    }

    public static synchronized void init(String url, String user, String password, int poolSize)
            throws SQLException {
        if (instance == null) {
            instance = new DBConnectionPool(url, user, password, poolSize);
            LOG.info("Product module DB pool initialised.");
        }
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public Connection acquire() throws SQLException {
        Connection conn = pool.poll();
        if (conn == null || conn.isClosed()) {
            conn = createConnection();
        }
        return conn;
    }

    public void release(Connection conn) {
        if (conn != null) {
            if (!pool.offer(conn)) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }
}