package service;
 
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import model.*;
 
public class PricelistService {
 
    private static final int SCALE = 6;
    private static final RoundingMode RM = RoundingMode.HALF_UP;
 
    /**
     * Compute the effective price for a product given a pricelist.
     * Returns list_price if no matching rule found.
     *
     * @param product    The product variant
     * @param pricelist  The pricelist with rules loaded
     * @param qty        Quantity being ordered
     * @return           Final computed price (BigDecimal, never float)
     */
    public BigDecimal computePrice(ProductProduct product, ProductPricelist pricelist,
                                    BigDecimal qty, Long categId) {
        if (pricelist == null || pricelist.getItems() == null || pricelist.getItems().isEmpty()) {
            return product.getLstPrice();
        }
 
        LocalDateTime now = LocalDateTime.now();
        ProductPricelistItem matchedRule = findMatchingRule(
                pricelist.getItems(), product, qty, categId, now);
 
        if (matchedRule == null) {
            return product.getLstPrice();
        }
 
        return applyRule(matchedRule, product, pricelist);
    }
 
    // ── Rule Matching (Odoo priority: variant > product > category > global) ──
 
    private ProductPricelistItem findMatchingRule(List<ProductPricelistItem> items,
                                                   ProductProduct product,
                                                   BigDecimal qty, Long categId,
                                                   LocalDateTime now) {
        // Sort: most specific first (0_product_variant → 3_global)
        // Then by min_quantity DESC (higher qty threshold first)
        return items.stream()
            .filter(item -> isDateValid(item, now))
            .filter(item -> isQtyValid(item, qty))
            .filter(item -> isProductMatch(item, product, categId))
            .min(Comparator
                .comparing(ProductPricelistItem::getAppliedOn)         // "0_..." < "3_..."
                .thenComparing(Comparator.comparing(
                    ProductPricelistItem::getMinQuantity).reversed()))  // highest qty first
            .orElse(null);
    }
 
    private boolean isDateValid(ProductPricelistItem item, LocalDateTime now) {
        if (item.getDateStart() != null && now.isBefore(item.getDateStart())) return false;
        if (item.getDateEnd()   != null && now.isAfter(item.getDateEnd()))    return false;
        return true;
    }
 
    private boolean isQtyValid(ProductPricelistItem item, BigDecimal qty) {
        return qty.compareTo(item.getMinQuantity()) >= 0;
    }
 
    private boolean isProductMatch(ProductPricelistItem item,
                                    ProductProduct product, Long categId) {
        switch (item.getAppliedOn()) {
            case "3_global":
                return true;
            case "2_product_category":
                return categId != null && categId.equals(item.getCategId());
            case "1_product":
                return product.getProductTmplId().equals(item.getProductTmplId());
            case "0_product_variant":
                return product.getId().equals(item.getProductId());
            default:
                return false;
        }
    }
 
    // ── Price Computation ─────────────────────────────────────
 
    private BigDecimal applyRule(ProductPricelistItem rule, ProductProduct product,
                                  ProductPricelist pricelist) {
        BigDecimal basePrice = getBasePrice(rule, product);
        BigDecimal price;
 
        switch (rule.getComputePrice()) {
            case "fixed":
                price = rule.getFixedPrice();
                break;
 
            case "percentage":
                // price = base * (1 - discount/100)
                BigDecimal discountFactor = BigDecimal.ONE
                    .subtract(rule.getPercentPrice().divide(BigDecimal.valueOf(100), SCALE, RM));
                price = basePrice.multiply(discountFactor);
                break;
 
            case "formula":
                // price = base * (1 - priceDiscount/100) + priceSurcharge
                BigDecimal formulaFactor = BigDecimal.ONE
                    .subtract(rule.getPriceDiscount().divide(BigDecimal.valueOf(100), SCALE, RM));
                price = basePrice.multiply(formulaFactor).add(rule.getPriceSurcharge());
 
                // Apply rounding (e.g. round to nearest 0.05)
                if (rule.getPriceRound() != null &&
                    rule.getPriceRound().compareTo(BigDecimal.ZERO) > 0) {
                    price = applyRounding(price, rule.getPriceRound());
                }
 
                // Apply margin limits
                price = applyMarginLimits(price, basePrice, rule);
                break;
 
            default:
                price = basePrice;
        }
 
        return price.setScale(2, RM);
    }
 
    private BigDecimal getBasePrice(ProductPricelistItem rule, ProductProduct product) {
        switch (rule.getBase()) {
            case "standard_price": return product.getStandardPrice();
            case "list_price":     return product.getLstPrice();
            default:               return product.getLstPrice();
            // "pricelist" base → recursive call would be needed for nested pricelists
        }
    }
 
    private BigDecimal applyRounding(BigDecimal price, BigDecimal rounding) {
        if (rounding.compareTo(BigDecimal.ZERO) == 0) return price;
        BigDecimal divided = price.divide(rounding, 0, RoundingMode.HALF_UP);
        return divided.multiply(rounding);
    }
 
    private BigDecimal applyMarginLimits(BigDecimal price, BigDecimal basePrice,
                                          ProductPricelistItem rule) {
        if (rule.getPriceMinMargin() != null &&
            rule.getPriceMinMargin().compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal minPrice = basePrice.add(rule.getPriceMinMargin());
            if (price.compareTo(minPrice) < 0) price = minPrice;
        }
        if (rule.getPriceMaxMargin() != null &&
            rule.getPriceMaxMargin().compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal maxPrice = basePrice.add(rule.getPriceMaxMargin());
            if (price.compareTo(maxPrice) > 0) price = maxPrice;
        }
        return price;
    }
}
 