package model;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
 
public class ProductPricelistItem {
    private Long id;
    private Long pricelistId;
    private Long companyId;
 
    /**
     * Scope of rule:
     *   3_global           → applies to all products
     *   2_product_category → only products in this category
     *   1_product          → only this template
     *   0_product_variant  → only this variant
     */
    private String appliedOn = "3_global";
    private Long categId;
    private Long productTmplId;
    private Long productId;
 
    private BigDecimal minQuantity = BigDecimal.ZERO;
    private LocalDateTime dateStart;
    private LocalDateTime dateEnd;
 
    /**
     * Base price used for formula/percentage:
     *   list_price     → product sales price
     *   standard_price → product cost
     *   pricelist      → another pricelist's price
     */
    private String base = "list_price";
    private Long basePricelistId;
 
    /**
     * How price is computed:
     *   fixed      → use fixedPrice directly
     *   percentage → base * (1 - percentPrice/100)
     *   formula    → base * (1 - priceDiscount/100) + priceSurcharge, then rounded
     */
    private String computePrice = "fixed";
 
    private BigDecimal fixedPrice    = BigDecimal.ZERO;
    private BigDecimal percentPrice  = BigDecimal.ZERO;
    private BigDecimal priceDiscount = BigDecimal.ZERO;
    private BigDecimal priceSurcharge = BigDecimal.ZERO;
    private BigDecimal priceRound    = BigDecimal.ZERO;
    private BigDecimal priceMinMargin = BigDecimal.ZERO;
    private BigDecimal priceMaxMargin = BigDecimal.ZERO;
 
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPricelistId() { return pricelistId; }
    public void setPricelistId(Long pricelistId) { this.pricelistId = pricelistId; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public String getAppliedOn() { return appliedOn; }
    public void setAppliedOn(String appliedOn) { this.appliedOn = appliedOn; }
    public Long getCategId() { return categId; }
    public void setCategId(Long categId) { this.categId = categId; }
    public Long getProductTmplId() { return productTmplId; }
    public void setProductTmplId(Long productTmplId) { this.productTmplId = productTmplId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public BigDecimal getMinQuantity() { return minQuantity; }
    public void setMinQuantity(BigDecimal minQuantity) { this.minQuantity = minQuantity; }
    public LocalDateTime getDateStart() { return dateStart; }
    public void setDateStart(LocalDateTime dateStart) { this.dateStart = dateStart; }
    public LocalDateTime getDateEnd() { return dateEnd; }
    public void setDateEnd(LocalDateTime dateEnd) { this.dateEnd = dateEnd; }
    public String getBase() { return base; }
    public void setBase(String base) { this.base = base; }
    public Long getBasePricelistId() { return basePricelistId; }
    public void setBasePricelistId(Long basePricelistId) { this.basePricelistId = basePricelistId; }
    public String getComputePrice() { return computePrice; }
    public void setComputePrice(String computePrice) { this.computePrice = computePrice; }
    public BigDecimal getFixedPrice() { return fixedPrice; }
    public void setFixedPrice(BigDecimal fixedPrice) { this.fixedPrice = fixedPrice; }
    public BigDecimal getPercentPrice() { return percentPrice; }
    public void setPercentPrice(BigDecimal percentPrice) { this.percentPrice = percentPrice; }
    public BigDecimal getPriceDiscount() { return priceDiscount; }
    public void setPriceDiscount(BigDecimal priceDiscount) { this.priceDiscount = priceDiscount; }
    public BigDecimal getPriceSurcharge() { return priceSurcharge; }
    public void setPriceSurcharge(BigDecimal priceSurcharge) { this.priceSurcharge = priceSurcharge; }
    public BigDecimal getPriceRound() { return priceRound; }
    public void setPriceRound(BigDecimal priceRound) { this.priceRound = priceRound; }
    public BigDecimal getPriceMinMargin() { return priceMinMargin; }
    public void setPriceMinMargin(BigDecimal priceMinMargin) { this.priceMinMargin = priceMinMargin; }
    public BigDecimal getPriceMaxMargin() { return priceMaxMargin; }
    public void setPriceMaxMargin(BigDecimal priceMaxMargin) { this.priceMaxMargin = priceMaxMargin; }
}