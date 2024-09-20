package org.shvk.productcatalogservice.exception;

public class ProductCatalogNotFoundException extends RuntimeException {
    public ProductCatalogNotFoundException(String message) {
        super(message);
    }
}