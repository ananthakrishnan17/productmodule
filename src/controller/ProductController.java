package controller;
 
import com.google.gson.*;
import model.*;
import service.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;
 
/**
 * GET    /api/products              → list all products (paginated)
 * GET    /api/products?pos=true     → list POS-available products
 * GET    /api/products/{id}         → get single product
 * GET    /api/products/barcode/{bc} → find by barcode
 * POST   /api/products              → create product
 * PUT    /api/products/{id}         → update product
 * DELETE /api/products/{id}         → soft delete
 */
@WebServlet(urlPatterns = {"/api/products", "/api/products/*"})
public class ProductController extends HttpServlet {
 
    private static final Logger LOG = Logger.getLogger(ProductController.class.getName());
    private static final Gson GSON = util.GsonUtil.GSON;
 
    private ProductService productService;
 
    @Override
    public void init() throws ServletException {
        productService = ServiceLocator.getProductService();
    }
 
    // ── GET ────────────────────────────────────────────────────
 
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String pathInfo = req.getPathInfo();
 
        try {
            // GET /api/products/barcode/{barcode}
            if (pathInfo != null && pathInfo.startsWith("/barcode/")) {
                String barcode = pathInfo.substring("/barcode/".length());
                ProductProduct p = productService.findByBarcode(barcode);
                sendJson(resp, 200, buildVariantJson(p));
                return;
            }
 
            // GET /api/products/{id}
            if (pathInfo != null && pathInfo.length() > 1) {
                long id = Long.parseLong(pathInfo.substring(1));
                ProductTemplate t = productService.getById(id);
                sendJson(resp, 200, buildTemplateJson(t));
                return;
            }
 
            // GET /api/products?pos=true&companyId=1&page=1&pageSize=50
            String posParam     = req.getParameter("pos");
            String companyParam = req.getParameter("companyId");
            long companyId = companyParam != null ? Long.parseLong(companyParam) : 1L;
 
            if ("true".equals(posParam)) {
                // POS-specific load — used by POS frontend
                List<ProductProduct> products = productService.getAllForPos(companyId);
                JsonObject body = new JsonObject();
                body.addProperty("status", "ok");
                body.add("products", GSON.toJsonTree(products));
                sendJson(resp, 200, body);
            } else {
                int page     = parseIntParam(req.getParameter("page"), 1);
                int pageSize = parseIntParam(req.getParameter("pageSize"), 50);
                List<ProductTemplate> templates = productService.getAll(companyId, true, page, pageSize);
                JsonObject body = new JsonObject();
                body.addProperty("status", "ok");
                body.add("products", GSON.toJsonTree(templates));
                sendJson(resp, 200, body);
            }
 
        } catch (NumberFormatException e) {
            sendError(resp, 400, "Invalid ID format");
        } catch (PosServiceException e) {
            sendError(resp, 404, e.getMessage());
        } catch (Exception e) {
            LOG.severe("Product GET error: " + e.getMessage());
            sendError(resp, 500, e.getMessage());
        }
    }
 
    // ── POST ───────────────────────────────────────────────────
 
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
 
        try {
            ProductTemplate template = GSON.fromJson(req.getReader(), ProductTemplate.class);
            if (template == null) {
                sendError(resp, 400, "Invalid request body");
                return;
            }
            // Default company from session config if not provided
            if (template.getCompanyId() == null) template.setCompanyId(1L);
 
            long id = productService.createProduct(template);
 
            JsonObject body = new JsonObject();
            body.addProperty("status", "ok");
            body.addProperty("id", id);
            sendJson(resp, 201, body);
 
        } catch (PosServiceException e) {
            sendError(resp, 422, e.getMessage());
        } catch (Exception e) {
            LOG.severe("Product POST error: " + e.getMessage());
            sendError(resp, 500, e.getMessage());
        }
    }
 
    // ── PUT ────────────────────────────────────────────────────
 
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
 
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                sendError(resp, 400, "Product ID required");
                return;
            }
            long id = Long.parseLong(pathInfo.substring(1));
            ProductTemplate template = GSON.fromJson(req.getReader(), ProductTemplate.class);
            if (template == null) {
                sendError(resp, 400, "Invalid request body");
                return;
            }
            template.setId(id);
            productService.updateProduct(template);
 
            JsonObject body = new JsonObject();
            body.addProperty("status", "ok");
            body.addProperty("message", "Product updated");
            sendJson(resp, 200, body);
 
        } catch (PosServiceException e) {
            sendError(resp, 422, e.getMessage());
        } catch (Exception e) {
            LOG.severe("Product PUT error: " + e.getMessage());
            sendError(resp, 500, e.getMessage());
        }
    }
 
    // ── DELETE ─────────────────────────────────────────────────
 
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
 
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                sendError(resp, 400, "Product ID required");
                return;
            }
            long id = Long.parseLong(pathInfo.substring(1));
            productService.deleteProduct(id);
 
            JsonObject body = new JsonObject();
            body.addProperty("status", "ok");
            body.addProperty("message", "Product deactivated");
            sendJson(resp, 200, body);
 
        } catch (PosServiceException e) {
            sendError(resp, 422, e.getMessage());
        } catch (Exception e) {
            LOG.severe("Product DELETE error: " + e.getMessage());
            sendError(resp, 500, e.getMessage());
        }
    }
 
    // ── Helpers ────────────────────────────────────────────────
 
    private JsonObject buildTemplateJson(ProductTemplate t) {
        JsonObject body = new JsonObject();
        body.addProperty("status", "ok");
        body.add("product", GSON.toJsonTree(t));
        return body;
    }
 
    private JsonObject buildVariantJson(ProductProduct p) {
        JsonObject body = new JsonObject();
        body.addProperty("status", "ok");
        body.add("product", GSON.toJsonTree(p));
        return body;
    }
 
    private void sendJson(HttpServletResponse resp, int status, JsonObject body) throws IOException {
        resp.setStatus(status);
        resp.getWriter().write(GSON.toJson(body));
    }
 
    private void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        JsonObject err = new JsonObject();
        err.addProperty("status", "error");
        err.addProperty("message", message);
        resp.getWriter().write(GSON.toJson(err));
    }
 
    private int parseIntParam(String value, int defaultVal) {
        if (value == null) return defaultVal;
        try { return Integer.parseInt(value); }
        catch (NumberFormatException e) { return defaultVal; }
    }
}