package controller;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.logging.Logger;

@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(AppContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            String dbUrl      = getParam(sce, "db.url",      "jdbc:postgresql://localhost:5432/pos_db");
            String dbUser     = getParam(sce, "db.user",     "pos_user");
            String dbPassword = getParam(sce, "db.password", "pos_pass");
            int    poolSize   = Integer.parseInt(getParam(sce, "db.pool.size", "10"));

            LOG.info("Product module connecting to: " + dbUrl);
            Class.forName("org.postgresql.Driver");
            ServiceLocator.init(dbUrl, dbUser, dbPassword, poolSize);
            LOG.info("Product module started successfully.");

        } catch (ClassNotFoundException e) {
            LOG.severe("JDBC Driver not found: " + e.getMessage());
        } catch (java.sql.SQLException e) {
            LOG.severe("DB Connection FAILED: " + e.getMessage());
        } catch (Exception e) {
            LOG.severe("Startup error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOG.info("Product module shutting down.");
    }

    private String getParam(ServletContextEvent sce, String name, String defaultValue) {
        String val = sce.getServletContext().getInitParameter(name);
        return (val != null && !val.isEmpty()) ? val : defaultValue;
    }
}