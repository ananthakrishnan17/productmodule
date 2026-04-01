package service;
 
import model.*;
import repository.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
 
public class ProductService {
 
    private static final Logger LOG = Logger.getLogger(ProductService.class.getName());
 
    private final DBConnectionPool pool;
    private final ProductDAO productDAO;
 
    public ProductService(DBConnectionPool pool, ProductDAO productDAO) {
        this.pool = pool;
        this.productDAO = productDAO;
    }
 
    // ── CREATE ─────────────────────────────────────────────────
 
    public long createProduct(ProductTemplate template) throws PosServiceException {
        validate(template);
        Connection conn = null;
        try {
            conn = pool.acquire();
            conn.setAutoCommit(false);
 
            long tmplId = productDAO.createTemplate(conn, template);
 
            // Auto-create default variant (like Odoo's _create_product_variant)
            ProductProduct defaultVariant = new ProductProduct();
            defaultVariant.setProductTmplId(tmplId);
            defaultVariant.setDefaultCode(template.getInternalRef());
            defaultVariant.setPriceExtra(java.math.BigDecimal.ZERO);
            productDAO.createVariant(conn, defaultVariant);
 
            conn.commit();
            LOG.info("Created product template id=" + tmplId + " name=" + template.getName());
            return tmplId;
 
        } catch (SQLException e) {
            rollback(conn);
            if (e.getMessage().contains("idx_product_barcode")) {
                throw new PosServiceException("Barcode already exists for another product.");
            }
            throw new PosServiceException("Failed to create product: " + e.getMessage(), e);
        } finally {
            resetAndRelease(conn);
        }
    }
 
    // ── READ ───────────────────────────────────────────────────
 
    public ProductTemplate getById(long id) throws PosServiceException {
        Connection conn = null;
        try {
            conn = pool.acquire();
            ProductTemplate t = productDAO.findTemplateById(conn, id);
            if (t == null) throw new PosServiceException("Product not found: " + id);
            return t;
        } catch (SQLException e) {
            throw new PosServiceException("DB error: " + e.getMessage(), e);
        } finally {
            pool.release(conn);
        }
    }
 
    public List<ProductProduct> getAllForPos(long companyId) throws PosServiceException {
        Connection conn = null;
        try {
            conn = pool.acquire();
            return productDAO.findAllForPos(conn, companyId);
        } catch (SQLException e) {
            throw new PosServiceException("DB error: " + e.getMessage(), e);
        } finally {
            pool.release(conn);
        }
    }
 
    public List<ProductTemplate> getAll(long companyId, boolean activeOnly,
                                         int page, int pageSize) throws PosServiceException {
        Connection conn = null;
        try {
            conn = pool.acquire();
            int offset = (page - 1) * pageSize;
            return productDAO.findAll(conn, companyId, activeOnly, offset, pageSize);
        } catch (SQLException e) {
            throw new PosServiceException("DB error: " + e.getMessage(), e);
        } finally {
            pool.release(conn);
        }
    }
 
    public ProductProduct findByBarcode(String barcode) throws PosServiceException {
        Connection conn = null;
        try {
            conn = pool.acquire();
            ProductProduct p = productDAO.findVariantByBarcode(conn, barcode);
            if (p == null) throw new PosServiceException("No product found with barcode: " + barcode);
            return p;
        } catch (SQLException e) {
            throw new PosServiceException("DB error: " + e.getMessage(), e);
        } finally {
            pool.release(conn);
        }
    }
 
    // ── UPDATE ─────────────────────────────────────────────────
 
    public void updateProduct(ProductTemplate template) throws PosServiceException {
        validate(template);
        Connection conn = null;
        try {
            conn = pool.acquire();
            conn.setAutoCommit(false);
            productDAO.updateTemplate(conn, template);
            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            throw new PosServiceException("Failed to update product: " + e.getMessage(), e);
        } finally {
            resetAndRelease(conn);
        }
    }
 
    // ── DELETE (soft) ──────────────────────────────────────────
 
    public void deleteProduct(long id) throws PosServiceException {
        Connection conn = null;
        try {
            conn = pool.acquire();
            conn.setAutoCommit(false);
            // Odoo rule: soft delete only — set active = FALSE
            productDAO.softDelete(conn, id);
            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            throw new PosServiceException("Failed to delete product: " + e.getMessage(), e);
        } finally {
            resetAndRelease(conn);
        }
    }
 
    // ── CATEGORY ──────────────────────────────────────────────
 
    public long createCategory(ProductCategory cat) throws PosServiceException {
        if (cat.getName() == null || cat.getName().isBlank()) {
            throw new PosServiceException("Category name is required.");
        }
        // Odoo rule: prevent recursive categories
        if (cat.getParentId() != null && cat.getParentId().equals(cat.getId())) {
            throw new PosServiceException("Cannot create recursive categories.");
        }
        Connection conn = null;
        try {
            conn = pool.acquire();
            return productDAO.createCategory(conn, cat);
        } catch (SQLException e) {
            throw new PosServiceException("Failed to create category: " + e.getMessage(), e);
        } finally {
            pool.release(conn);
        }
    }
 
    public List<ProductCategory> getAllCategories() throws PosServiceException {
        Connection conn = null;
        try {
            conn = pool.acquire();
            return productDAO.findAllCategories(conn);
        } catch (SQLException e) {
            throw new PosServiceException("DB error: " + e.getMessage(), e);
        } finally {
            pool.release(conn);
        }
    }
 
    // ── VALIDATION (Business Rules) ────────────────────────────
 
    private void validate(ProductTemplate t) throws PosServiceException {
        if (t.getName() == null || t.getName().isBlank()) {
            throw new PosServiceException("Product name is required.");
        }
        if (t.getListPrice() == null || t.getListPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new PosServiceException("Sales price must be zero or positive.");
        }
        if (t.getStandardPrice() == null || t.getStandardPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new PosServiceException("Cost price must be zero or positive.");
        }
        if (!List.of("consu", "service", "combo").contains(t.getType())) {
            throw new PosServiceException("Invalid product type: " + t.getType());
        }
        if (t.getCompanyId() == null) {
            throw new PosServiceException("Company ID is required.");
        }
    }
 
    // ── Helpers ────────────────────────────────────────────────
 
    private void rollback(Connection conn) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException ex) {
                LOG.severe("Rollback failed: " + ex.getMessage());
            }
        }
    }
 
    private void resetAndRelease(Connection conn) {
        if (conn != null) {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            pool.release(conn);
        }
    }
}
 