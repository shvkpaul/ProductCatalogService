package org.shvk.productcatalogservice.service;

import org.shvk.productcatalogservice.model.ProductCatalogRequest;
import org.shvk.productcatalogservice.model.ProductCatalogResponse;

public interface ProductCatalogService {
    long addProduct(ProductCatalogRequest productCatalogRequest);

    ProductCatalogResponse getProductById(long productId);

    void reduceQuantity(long productId, long quantity);
}
