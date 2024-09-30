package org.shvk.productcatalogservice.controller;

import org.shvk.productcatalogservice.model.ProductCatalogRequest;
import org.shvk.productcatalogservice.model.ProductCatalogResponse;
import org.shvk.productcatalogservice.service.ProductCatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductCatalogController {

    private ProductCatalogService productCatalogService;

    public ProductCatalogController(ProductCatalogService productCatalogService) {
        this.productCatalogService = productCatalogService;
    }

    @PostMapping
    public ResponseEntity<String> addProduct(@RequestBody ProductCatalogRequest productCatalogRequest) {
        String message = productCatalogService.addProduct(productCatalogRequest);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductCatalogResponse> getProductById(
            @PathVariable("id") long productId
    ) {
        ProductCatalogResponse productCatalogResponse =
                productCatalogService.getProductById(productId);

        return new ResponseEntity<>(productCatalogResponse, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductCatalogResponse>> getAllProduct() {
        List<ProductCatalogResponse> productCatalogResponse =
                productCatalogService.getAllProduct();

        return new ResponseEntity<>(productCatalogResponse, HttpStatus.OK);
    }

    @PutMapping("/reduceQuantity/{id}")
    public ResponseEntity<String> reduceQuantity(
            @PathVariable("id") long productId,
            @RequestParam long quantity
    ) {
        productCatalogService.reduceQuantity(productId, quantity);
        return new ResponseEntity<>("Product quantity updated successfully", HttpStatus.OK);
    }
}
