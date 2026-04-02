package controller;

import repository.*;
import service.*;

import java.sql.Connection;
import java.sql.SQLException;

public class ServiceLocator {

    private static DBConnectionPool pool;
    private static repository.ProductDAO productDAO;
    private static service.ProductService productService;
    private static service.PricelistService pricelistService;

    public static void init(String dbUrl, String dbUser, String dbPassword, int poolSize)
            throws SQLException {
        DBConnectionPool.init(dbUrl, dbUser, dbPassword, poolSize);
        pool = DBConnectionPool.getInstance();

        productDAO       = new repository.ProductDAO();
        pricelistService = new service.PricelistService();
        productService   = new service.ProductService(pool, productDAO);

        java.util.logging.Logger.getLogger(ServiceLocator.class.getName())
            .info("Product module services initialised.");
    }

    public static DBConnectionPool getPool()                       { return pool; }
    public static service.ProductService getProductService()       { return productService; }
    public static service.PricelistService getPricelistService()   { return pricelistService; }

    public static Connection getConnection() throws SQLException {
        return pool.acquire();
    }
}