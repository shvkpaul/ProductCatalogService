package org.shvk.productcatalogservice.model;

import lombok.Data;

@Data
public class ProductCatalogRequest {
    private String name;
    private long price;
    private long quantity;
}
