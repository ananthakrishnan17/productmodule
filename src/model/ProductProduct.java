package model;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
 
public class ProductProduct {
    private Long id;
    private Long productTmplId;
    private String defaultCode;
    private String barcode;
    /** Price added on top of template list_price */
    private BigDecimal priceExtra = BigDecimal.ZERO;
    private boolean active = true;
    private LocalDateTime createdAt;
 
    // Denormalized fields from template (for POS convenience)
    private String name;
    private BigDecimal lstPrice;
    private BigDecimal standardPrice;
    private String type;
    private String uomName;
    private boolean availableInPos;
 
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductTmplId() { return productTmplId; }
    public void setProductTmplId(Long productTmplId) { this.productTmplId = productTmplId; }
    public String getDefaultCode() { return defaultCode; }
    public void setDefaultCode(String defaultCode) { this.defaultCode = defaultCode; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public BigDecimal getPriceExtra() { return priceExtra; }
    public void setPriceExtra(BigDecimal priceExtra) { this.priceExtra = priceExtra; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getLstPrice() { return lstPrice; }
    public void setLstPrice(BigDecimal lstPrice) { this.lstPrice = lstPrice; }
    public BigDecimal getStandardPrice() { return standardPrice; }
    public void setStandardPrice(BigDecimal standardPrice) { this.standardPrice = standardPrice; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getUomName() { return uomName; }
    public void setUomName(String uomName) { this.uomName = uomName; }
    public boolean isAvailableInPos() { return availableInPos; }
    public void setAvailableInPos(boolean availableInPos) { this.availableInPos = availableInPos; }
}
 