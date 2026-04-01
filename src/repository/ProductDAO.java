// ============================================================
// FILE: src/repository/ProductDAO.java
// ============================================================
package repository;

import model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    // ── CREATE ─────────────────────────────────────────────────

    public long createTemplate(Connection conn, ProductTemplate t) throws SQLException {
        String sql = """
            INSERT INTO product_template (
                name, description, description_sale, internal_ref,
                type, list_price, standard_price,
                categ_id, uom_name, sale_ok, available_in_pos,
                active, tracking, cost_method, image_url,
                pos_sequence, pos_categ_id, company_id
            ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            RETURNING id
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, t.getName());
            ps.setString(i++, t.getDescription());
            ps.setString(i++, t.getDescriptionSale());
            ps.setString(i++, t.getInternalRef());
            ps.setString(i++, t.getType());
            ps.setBigDecimal(i++, t.getListPrice());
            ps.setBigDecimal(i++, t.getStandardPrice());
            setNullableLong(ps, i++, t.getCategId());
            ps.setString(i++, t.getUomName());
            ps.setBoolean(i++, t.isSaleOk());
            ps.setBoolean(i++, t.isAvailableInPos());
            ps.setBoolean(i++, t.isActive());
            ps.setString(i++, t.getTracking());
            ps.setString(i++, t.getCostMethod());
            ps.setString(i++, t.getImageUrl());
            ps.setInt(i++, t.getPosSequence());
            setNullableLong(ps, i++, t.getPosCategId());
            ps.setLong(i++, t.getCompanyId());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    // Save tax relationships
                    if (t.getTaxIds() != null) saveTaxRel(conn, id, t.getTaxIds());
                    return id;
                }
                throw new SQLException("INSERT product_template returned no ID");
            }
        }
    }

    public long createVariant(Connection conn, ProductProduct v) throws SQLException {
        String sql = """
            INSERT INTO product_product (product_tmpl_id, default_code, barcode, price_extra, active)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, v.getProductTmplId());
            ps.setString(2, v.getDefaultCode());
            ps.setString(3, v.getBarcode());
            ps.setBigDecimal(4, v.getPriceExtra());
            ps.setBoolean(5, v.isActive());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
                throw new SQLException("INSERT product_product returned no ID");
            }
        }
    }

    // ── READ ───────────────────────────────────────────────────

    public ProductTemplate findTemplateById(Connection conn, long id) throws SQLException {
        String sql = "SELECT * FROM product_template WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ProductTemplate t = mapTemplate(rs);
                    t.setTaxIds(findTaxIdsByTemplate(conn, id));
                    t.setVariants(findVariantsByTemplate(conn, id));
                    return t;
                }
                return null;
            }
        }
    }

    /**
     * Used by POS session load — returns all active POS products.
     * Mirrors Odoo's _load_pos_data_domain for product.product.
     */
    public List<ProductProduct> findAllForPos(Connection conn, long companyId) throws SQLException {
        String sql = """
            SELECT pp.*, pt.name, pt.list_price AS lst_price, pt.standard_price,
                   pt.type, pt.uom_name, pt.available_in_pos,
                   pt.description_sale, pt.image_url, pt.pos_sequence,
                   pt.categ_id, pt.id AS tmpl_id
            FROM product_product pp
            JOIN product_template pt ON pp.product_tmpl_id = pt.id
            WHERE pp.active = TRUE
              AND pt.active = TRUE
              AND pt.available_in_pos = TRUE
              AND pt.sale_ok = TRUE
              AND pt.company_id = ?
            ORDER BY pt.pos_sequence ASC, pt.name ASC
            """;
        List<ProductProduct> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductProduct p = mapVariant(rs);
                    p.setLstPrice(rs.getBigDecimal("lst_price").add(p.getPriceExtra()));
                    list.add(p);
                }
            }
        }
        return list;
    }

    public List<ProductTemplate> findAll(Connection conn, long companyId,
                                          boolean activeOnly, int offset, int limit) throws SQLException {
        String sql = """
            SELECT * FROM product_template
            WHERE company_id = ?
            """ + (activeOnly ? " AND active = TRUE" : "") + """
            ORDER BY name ASC
            LIMIT ? OFFSET ?
            """;
        List<ProductTemplate> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, companyId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductTemplate t = mapTemplate(rs);
                    t.setTaxIds(findTaxIdsByTemplate(conn, t.getId()));
                    list.add(t);
                }
            }
        }
        return list;
    }

    public ProductProduct findVariantByBarcode(Connection conn, String barcode) throws SQLException {
        String sql = """
            SELECT pp.*, pt.name, pt.list_price AS lst_price, pt.standard_price,
                   pt.type, pt.uom_name, pt.available_in_pos
            FROM product_product pp
            JOIN product_template pt ON pp.product_tmpl_id = pt.id
            WHERE pp.barcode = ? AND pp.active = TRUE
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, barcode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapVariant(rs);
                return null;
            }
        }
    }

    // ── UPDATE ─────────────────────────────────────────────────

    public void updateTemplate(Connection conn, ProductTemplate t) throws SQLException {
        String sql = """
            UPDATE product_template SET
                name = ?, description = ?, description_sale = ?,
                type = ?, list_price = ?, standard_price = ?,
                categ_id = ?, available_in_pos = ?, active = ?,
                image_url = ?, pos_sequence = ?, updated_at = NOW()
            WHERE id = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, t.getName());
            ps.setString(i++, t.getDescription());
            ps.setString(i++, t.getDescriptionSale());
            ps.setString(i++, t.getType());
            ps.setBigDecimal(i++, t.getListPrice());
            ps.setBigDecimal(i++, t.getStandardPrice());
            setNullableLong(ps, i++, t.getCategId());
            ps.setBoolean(i++, t.isAvailableInPos());
            ps.setBoolean(i++, t.isActive());
            ps.setString(i++, t.getImageUrl());
            ps.setInt(i++, t.getPosSequence());
            ps.setLong(i++, t.getId());
            ps.executeUpdate();
        }
        // Refresh tax relations
        if (t.getTaxIds() != null) {
            deleteTaxRel(conn, t.getId());
            saveTaxRel(conn, t.getId(), t.getTaxIds());
        }
    }

    // ── DELETE (soft) ──────────────────────────────────────────

    public void softDelete(Connection conn, long templateId) throws SQLException {
        String sql = "UPDATE product_template SET active = FALSE, updated_at = NOW() WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, templateId);
            ps.executeUpdate();
        }
    }

    // ── CATEGORY ──────────────────────────────────────────────

    public long createCategory(Connection conn, ProductCategory cat) throws SQLException {
        // Build complete_name and parent_path
        String completeName = cat.getName();
        String parentPath   = "";
        if (cat.getParentId() != null) {
            ProductCategory parent = findCategoryById(conn, cat.getParentId());
            if (parent != null) {
                completeName = parent.getCompleteName() + " / " + cat.getName();
                parentPath   = parent.getParentPath() + parent.getId() + "/";
            }
        }

        String sql = """
            INSERT INTO product_category (name, complete_name, parent_id, parent_path, sequence)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cat.getName());
            ps.setString(2, completeName);
            setNullableLong(ps, 3, cat.getParentId());
            ps.setString(4, parentPath);
            ps.setInt(5, cat.getSequence());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
                throw new SQLException("INSERT product_category returned no ID");
            }
        }
    }

    public ProductCategory findCategoryById(Connection conn, long id) throws SQLException {
        String sql = "SELECT * FROM product_category WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapCategory(rs);
                return null;
            }
        }
    }

    public List<ProductCategory> findAllCategories(Connection conn) throws SQLException {
        String sql = "SELECT * FROM product_category WHERE active = TRUE ORDER BY complete_name ASC";
        List<ProductCategory> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapCategory(rs));
        }
        return list;
    }

    // ── PRICELIST ─────────────────────────────────────────────

    public List<ProductPricelist> findActivePricelists(Connection conn, long companyId) throws SQLException {
        String sql = """
            SELECT * FROM product_pricelist
            WHERE active = TRUE AND (company_id = ? OR company_id IS NULL)
            ORDER BY sequence ASC
            """;
        List<ProductPricelist> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapPricelist(rs));
            }
        }
        return list;
    }

    public List<ProductPricelistItem> findPricelistItems(Connection conn, long pricelistId) throws SQLException {
        String sql = """
            SELECT * FROM product_pricelist_item
            WHERE pricelist_id = ?
            ORDER BY applied_on ASC, min_quantity DESC
            """;
        List<ProductPricelistItem> items = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, pricelistId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) items.add(mapPricelistItem(rs));
            }
        }
        return items;
    }

    // ── HELPERS ────────────────────────────────────────────────

    private List<Long> findTaxIdsByTemplate(Connection conn, long tmplId) throws SQLException {
        String sql = "SELECT tax_id FROM product_taxes_rel WHERE product_tmpl_id = ?";
        List<Long> ids = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, tmplId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getLong(1));
            }
        }
        return ids;
    }

    private List<ProductProduct> findVariantsByTemplate(Connection conn, long tmplId) throws SQLException {
        String sql = "SELECT * FROM product_product WHERE product_tmpl_id = ? AND active = TRUE";
        List<ProductProduct> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, tmplId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapVariantSimple(rs));
            }
        }
        return list;
    }

    private void saveTaxRel(Connection conn, long tmplId, List<Long> taxIds) throws SQLException {
        String sql = "INSERT INTO product_taxes_rel (product_tmpl_id, tax_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Long taxId : taxIds) {
                ps.setLong(1, tmplId);
                ps.setLong(2, taxId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void deleteTaxRel(Connection conn, long tmplId) throws SQLException {
        String sql = "DELETE FROM product_taxes_rel WHERE product_tmpl_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, tmplId);
            ps.executeUpdate();
        }
    }

    // ── MAPPERS ────────────────────────────────────────────────

    private ProductTemplate mapTemplate(ResultSet rs) throws SQLException {
        ProductTemplate t = new ProductTemplate();
        t.setId(rs.getLong("id"));
        t.setName(rs.getString("name"));
        t.setDescription(rs.getString("description"));
        t.setDescriptionSale(rs.getString("description_sale"));
        t.setInternalRef(rs.getString("internal_ref"));
        t.setType(rs.getString("type"));
        t.setListPrice(rs.getBigDecimal("list_price"));
        t.setStandardPrice(rs.getBigDecimal("standard_price"));
        long categId = rs.getLong("categ_id");
        if (!rs.wasNull()) t.setCategId(categId);
        t.setUomName(rs.getString("uom_name"));
        t.setSaleOk(rs.getBoolean("sale_ok"));
        t.setAvailableInPos(rs.getBoolean("available_in_pos"));
        t.setActive(rs.getBoolean("active"));
        t.setTracking(rs.getString("tracking"));
        t.setCostMethod(rs.getString("cost_method"));
        t.setImageUrl(rs.getString("image_url"));
        t.setPosSequence(rs.getInt("pos_sequence"));
        t.setCompanyId(rs.getLong("company_id"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) t.setCreatedAt(ca.toLocalDateTime());
        return t;
    }

    private ProductProduct mapVariant(ResultSet rs) throws SQLException {
        ProductProduct p = new ProductProduct();
        p.setId(rs.getLong("id"));
        p.setProductTmplId(rs.getLong("product_tmpl_id"));
        p.setDefaultCode(rs.getString("default_code"));
        p.setBarcode(rs.getString("barcode"));
        p.setPriceExtra(rs.getBigDecimal("price_extra"));
        p.setActive(rs.getBoolean("active"));
        p.setName(rs.getString("name"));
        p.setStandardPrice(rs.getBigDecimal("standard_price"));
        p.setType(rs.getString("type"));
        p.setUomName(rs.getString("uom_name"));
        p.setAvailableInPos(rs.getBoolean("available_in_pos"));
        return p;
    }

    private ProductProduct mapVariantSimple(ResultSet rs) throws SQLException {
        ProductProduct p = new ProductProduct();
        p.setId(rs.getLong("id"));
        p.setProductTmplId(rs.getLong("product_tmpl_id"));
        p.setDefaultCode(rs.getString("default_code"));
        p.setBarcode(rs.getString("barcode"));
        p.setPriceExtra(rs.getBigDecimal("price_extra"));
        p.setActive(rs.getBoolean("active"));
        return p;
    }

    private ProductCategory mapCategory(ResultSet rs) throws SQLException {
        ProductCategory c = new ProductCategory();
        c.setId(rs.getLong("id"));
        c.setName(rs.getString("name"));
        c.setCompleteName(rs.getString("complete_name"));
        long parentId = rs.getLong("parent_id");
        if (!rs.wasNull()) c.setParentId(parentId);
        c.setParentPath(rs.getString("parent_path"));
        c.setSequence(rs.getInt("sequence"));
        c.setActive(rs.getBoolean("active"));
        return c;
    }

    private ProductPricelist mapPricelist(ResultSet rs) throws SQLException {
        ProductPricelist p = new ProductPricelist();
        p.setId(rs.getLong("id"));
        p.setName(rs.getString("name"));
        p.setCurrencyId(rs.getLong("currency_id"));
        long companyId = rs.getLong("company_id");
        if (!rs.wasNull()) p.setCompanyId(companyId);
        p.setSequence(rs.getInt("sequence"));
        p.setActive(rs.getBoolean("active"));
        return p;
    }

    private ProductPricelistItem mapPricelistItem(ResultSet rs) throws SQLException {
        ProductPricelistItem item = new ProductPricelistItem();
        item.setId(rs.getLong("id"));
        item.setPricelistId(rs.getLong("pricelist_id"));
        item.setAppliedOn(rs.getString("applied_on"));
        long categId = rs.getLong("categ_id");
        if (!rs.wasNull()) item.setCategId(categId);
        long tmplId = rs.getLong("product_tmpl_id");
        if (!rs.wasNull()) item.setProductTmplId(tmplId);
        item.setMinQuantity(rs.getBigDecimal("min_quantity"));
        item.setBase(rs.getString("base"));
        item.setComputePrice(rs.getString("compute_price"));
        item.setFixedPrice(rs.getBigDecimal("fixed_price"));
        item.setPercentPrice(rs.getBigDecimal("percent_price"));
        item.setPriceDiscount(rs.getBigDecimal("price_discount"));
        item.setPriceSurcharge(rs.getBigDecimal("price_surcharge"));
        item.setPriceRound(rs.getBigDecimal("price_round"));
        Timestamp ds = rs.getTimestamp("date_start");
        if (ds != null) item.setDateStart(ds.toLocalDateTime());
        Timestamp de = rs.getTimestamp("date_end");
        if (de != null) item.setDateEnd(de.toLocalDateTime());
        return item;
    }

    private void setNullableLong(PreparedStatement ps, int idx, Long value) throws SQLException {
        if (value != null) ps.setLong(idx, value);
        else ps.setNull(idx, java.sql.Types.BIGINT);
    }
}