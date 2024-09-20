package org.shvk.productcatalogservice.service;

import lombok.extern.log4j.Log4j2;
import org.shvk.productcatalogservice.entity.Product;
import org.shvk.productcatalogservice.model.ProductCatalogRequest;
import org.shvk.productcatalogservice.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class ProductCatalogServiceImpl implements ProductCatalogService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public long addProduct(ProductCatalogRequest productCatalogRequest) {
        log.info("Adding product");

        Product product = Product.builder()
                .productName(productCatalogRequest.getName())
                .price(productCatalogRequest.getPrice())
                .quantity(productCatalogRequest.getQuantity())
                .build();

        productRepository.save(product);

        log.info("Product created");
        return product.getProductId();
    }
}
