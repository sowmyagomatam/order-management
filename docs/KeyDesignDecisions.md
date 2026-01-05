## Key design decisions

### Domain Entities

- OrderItem : Should Order item capture Product details or only a reference ?
How should we handle orders when products change or become unavailable over time?

Option A: Keep historical snapshot of some fields

```java
import java.math.BigDecimal;

@Entity
public class OrderItem {
    private String productId; // reference to product for queries/analytics
    private String productSku;  //snapshot at purchase time
    private String productName; //snapshot at purchase time
    private BigDecimal price; //snapshot at purchase time
}
```
Pros: Orders are immutable. Purchase history is maintained
Cons: Duplication of data

Option B: Reference Product from OrderItem

```java
import java.math.BigDecimal;

@Entity
public class OrderItem {
    private String productId; // reference to product for queries/analytics
    private BigDecimal productName; //snapshot at purchase time
}
```
Pros:
- De duplication of data. Always reference the Product entity

Cons:
- Order broken if product is unavailable

Option C: Hybrid (best of both)

```java
import java.math.BigDecimal;

@Entity
public class OrderItem {
    private String productId; // reference to product for queries/analytics
    private String productSku;  //snapshot at purchase time
    private String productName; //snapshot at purchase time
    private BigDecimal price; //snapshot at purchase time
    
    private String productMetadata; // full product info at purchase time say Category, brand etc
}
```

Pros:
- Order history is preserved with full snapshot of what was purchased
- For enrichment, we can use the reference to the product id