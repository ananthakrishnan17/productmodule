package controller;

import com.google.gson.*;
import model.ProductCategory;
import service.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

@WebServlet(urlPatterns = {"/api/categories", "/api/categories/*"})
public class CategoryController extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(CategoryController.class.getName());
    private static final Gson GSON = util.GsonUtil.GSON;

    private ProductService productService;

    @Override
    public void init() throws ServletException {

        productService = ServiceLocator.getProductService();

        if (productService == null) {
            LOG.severe("ProductService is NULL — DB connection failed during startup.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");

        if (productService == null) {
            resp.setStatus(503);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Service unavailable — DB not connected\"}");
            return;
        }

        try {

            List<ProductCategory> cats = productService.getAllCategories();

            JsonObject body = new JsonObject();
            body.addProperty("status", "ok");
            body.add("categories", GSON.toJsonTree(cats));

            resp.getWriter().write(GSON.toJson(body));

        } catch (PosServiceException e) {

            resp.setStatus(500);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");

        // DB startup fail check
        if (productService == null) {
            resp.setStatus(503);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Service unavailable — DB not connected\"}");
            return;
        }

        try {

            ProductCategory cat = GSON.fromJson(req.getReader(), ProductCategory.class);

            long id = productService.createCategory(cat);

            JsonObject body = new JsonObject();
            body.addProperty("status", "ok");
            body.addProperty("id", id);

            resp.setStatus(201);
            resp.getWriter().write(GSON.toJson(body));

        } catch (PosServiceException e) {

            resp.setStatus(422);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");

        } catch (Exception e) {

            LOG.severe("Category POST error: " + e.getMessage());
            e.printStackTrace();

            resp.setStatus(500);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }
}