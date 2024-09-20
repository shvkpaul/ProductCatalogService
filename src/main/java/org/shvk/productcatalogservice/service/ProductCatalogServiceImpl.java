package org.shvk.productcatalogservice.service;

import lombok.extern.log4j.Log4j2;
import org.shvk.productcatalogservice.entity.Product;
import org.shvk.productcatalogservice.exception.ProductCatalogNotFoundException;
import org.shvk.productcatalogservice.exception.ProductQuantityNotSufficientException;
import org.shvk.productcatalogservice.model.ProductCatalogRequest;
import org.shvk.productcatalogservice.model.ProductCatalogResponse;
import org.shvk.productcatalogservice.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class ProductCatalogServiceImpl implements ProductCatalogService {

    private ProductRepository productRepository;

    public ProductCatalogServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

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

    @Override
    public ProductCatalogResponse getProductById(long productId) {
        log.info("Getting product for productId: {}", productId);

        Product product = getProduct(productId);

        ProductCatalogResponse productCatalogResponse =
                ProductCatalogResponse.builder()
                        .productId(product.getProductId())
                        .price(product.getPrice())
                        .productName(product.getProductName())
                        .quantity(product.getQuantity())
                        .build();

        return productCatalogResponse;
    }

    @Override
    public void reduceQuantity(long productId, long quantity) {
        log.info("Reduce quantity {} for productId: {}", quantity, productId);

        Product product = getProduct(productId);

        if (product.getQuantity() < quantity) {
            throw new ProductQuantityNotSufficientException("Product does not have sufficient quantity");
        }

        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);
        log.info("Product quantity updated successfully");
    }

    private Product getProduct(long productId) {
        return productRepository.findById(productId)
                .orElseThrow(
                        () -> new ProductCatalogNotFoundException("Product not found"));
    }
}
