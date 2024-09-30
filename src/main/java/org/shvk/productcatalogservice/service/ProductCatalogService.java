package org.shvk.productcatalogservice.service;

import org.shvk.productcatalogservice.model.ProductCatalogRequest;
import org.shvk.productcatalogservice.model.ProductCatalogResponse;

import java.util.List;

public interface ProductCatalogService {
    String addProduct(ProductCatalogRequest productCatalogRequest);

    ProductCatalogResponse getProductById(long productId);

    void reduceQuantity(long productId, long quantity);

    List<ProductCatalogResponse> getAllProduct();

}
