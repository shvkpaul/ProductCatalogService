package org.shvk.productcatalogservice.exception;

public class ProductQuantityNotSufficientException extends RuntimeException {
    public ProductQuantityNotSufficientException(String message) {
        super(message);
    }
}