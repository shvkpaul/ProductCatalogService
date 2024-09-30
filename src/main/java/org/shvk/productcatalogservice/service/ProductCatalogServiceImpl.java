package org.shvk.productcatalogservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.shvk.productcatalogservice.entity.Product;
import org.shvk.productcatalogservice.exception.ProductCatalogNotFoundException;
import org.shvk.productcatalogservice.exception.ProductQuantityNotSufficientException;
import org.shvk.productcatalogservice.integration.KafkaProducer;
import org.shvk.productcatalogservice.model.ProductCatalogRequest;
import org.shvk.productcatalogservice.model.ProductCatalogResponse;
import org.shvk.productcatalogservice.repository.ProductRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
public class ProductCatalogServiceImpl implements ProductCatalogService {

    private ProductRepository productRepository;
    private KafkaProducer kafkaProducer;

    public ProductCatalogServiceImpl(ProductRepository productRepository, KafkaProducer kafkaProducer) {
        this.productRepository = productRepository;
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public String addProduct(ProductCatalogRequest productCatalogRequest) {
        log.info("Adding product");

        Product product = Product.builder()
                .productName(productCatalogRequest.getName())
                .price(productCatalogRequest.getPrice())
                .quantity(productCatalogRequest.getQuantity())
                .build();

        productRepository.save(product);
        log.info("Product created: {}", product.getProductId());

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String payload = objectMapper.writeValueAsString(product);

            kafkaProducer.sendMessage("product-topic", payload);
            log.info("Product creation message published: {}", product);
        } catch (Exception e) {
            log.error("Error in publishing message in product-topic :{}", e.getMessage());
        }

        return "Product creation request received";
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

    @Override
    @Cacheable(value = "products")
    public List<ProductCatalogResponse> getAllProduct() {
        log.info("Getting all product");

        List<Product> products = productRepository.findAll();

        return products.stream()
                .map(product -> ProductCatalogResponse.builder()
                        .productId(product.getProductId())
                        .price(product.getPrice())
                        .productName(product.getProductName())
                        .quantity(product.getQuantity())
                        .build())
                .collect(Collectors.toList());
    }

    private Product getProduct(long productId) {
        return productRepository.findById(productId)
                .orElseThrow(
                        () -> new ProductCatalogNotFoundException("Product not found"));
    }
}
