package org.shvk.productcatalogservice.service;

import org.shvk.productcatalogservice.model.ProductCatalogRequest;

public interface ProductCatalogService {
    long addProduct(ProductCatalogRequest productCatalogRequest);
}
