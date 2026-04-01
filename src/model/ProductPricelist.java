package model;
 
import java.util.List;
 
public class ProductPricelist {
    private Long id;
    private String name;
    private Long currencyId;
    private Long companyId;
    private int sequence = 16;
    private boolean active = true;
    private List<ProductPricelistItem> items;
 
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getCurrencyId() { return currencyId; }
    public void setCurrencyId(Long currencyId) { this.currencyId = currencyId; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public int getSequence() { return sequence; }
    public void setSequence(int sequence) { this.sequence = sequence; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<ProductPricelistItem> getItems() { return items; }
    public void setItems(List<ProductPricelistItem> items) { this.items = items; }
}
 