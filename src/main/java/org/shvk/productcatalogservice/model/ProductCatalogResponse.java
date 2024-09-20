package org.shvk.productcatalogservice.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductCatalogResponse {
    private long productId;
    private String productName;
    private long price;
    private long quantity;
}
