package model;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
 
public class ProductTemplate {
    private Long id;
    private String name;
    private String description;
    private String descriptionSale;
    private String internalRef;
 
    /** consu | service | combo */
    private String type = "consu";
 
    /** Sales price shown on POS */
    private BigDecimal listPrice  = BigDecimal.ZERO;
    /** Cost — used for margin calculation */
    private BigDecimal standardPrice = BigDecimal.ZERO;
 
    private Long categId;
    private String uomName = "Unit(s)";
 
    private boolean saleOk         = true;
    private boolean availableInPos  = true;
    private boolean active          = true;
    private boolean isFavorite      = false;
 
    /** none | lot | serial */
    private String tracking = "none";
 
    /** standard | fifo | average */
    private String costMethod = "standard";
 
    private String imageUrl;
    private int posSequence = 10;
    private Long posCategId;
    private Long companyId;
 
    /** Tax IDs applied to this product (loaded from product_taxes_rel) */
    private List<Long> taxIds;
 
    /** Loaded variants (usually just 1 for POS) */
    private List<ProductProduct> variants;
 
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
 
    // ── Getters & Setters ──────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDescriptionSale() { return descriptionSale; }
    public void setDescriptionSale(String descriptionSale) { this.descriptionSale = descriptionSale; }
    public String getInternalRef() { return internalRef; }
    public void setInternalRef(String internalRef) { this.internalRef = internalRef; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getListPrice() { return listPrice; }
    public void setListPrice(BigDecimal listPrice) { this.listPrice = listPrice; }
    public BigDecimal getStandardPrice() { return standardPrice; }
    public void setStandardPrice(BigDecimal standardPrice) { this.standardPrice = standardPrice; }
    public Long getCategId() { return categId; }
    public void setCategId(Long categId) { this.categId = categId; }
    public String getUomName() { return uomName; }
    public void setUomName(String uomName) { this.uomName = uomName; }
    public boolean isSaleOk() { return saleOk; }
    public void setSaleOk(boolean saleOk) { this.saleOk = saleOk; }
    public boolean isAvailableInPos() { return availableInPos; }
    public void setAvailableInPos(boolean availableInPos) { this.availableInPos = availableInPos; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public String getTracking() { return tracking; }
    public void setTracking(String tracking) { this.tracking = tracking; }
    public String getCostMethod() { return costMethod; }
    public void setCostMethod(String costMethod) { this.costMethod = costMethod; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public int getPosSequence() { return posSequence; }
    public void setPosSequence(int posSequence) { this.posSequence = posSequence; }
    public Long getPosCategId() { return posCategId; }
    public void setPosCategId(Long posCategId) { this.posCategId = posCategId; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public List<Long> getTaxIds() { return taxIds; }
    public void setTaxIds(List<Long> taxIds) { this.taxIds = taxIds; }
    public List<ProductProduct> getVariants() { return variants; }
    public void setVariants(List<ProductProduct> variants) { this.variants = variants; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
 