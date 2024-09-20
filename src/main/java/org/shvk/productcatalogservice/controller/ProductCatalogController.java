package org.shvk.productcatalogservice.controller;

import org.shvk.productcatalogservice.model.ProductCatalogRequest;
import org.shvk.productcatalogservice.model.ProductCatalogResponse;
import org.shvk.productcatalogservice.service.ProductCatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
public class ProductCatalogController {

    @Autowired
    private ProductCatalogService productCatalogService;

    @PostMapping
    public ResponseEntity<Long> addProduct(@RequestBody ProductCatalogRequest productCatalogRequest) {
        long productId = productCatalogService.addProduct(productCatalogRequest);
        return new ResponseEntity<>(productId, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductCatalogResponse> getProductById(
            @PathVariable("id") long productId
    ) {
        ProductCatalogResponse productCatalogResponse =
                productCatalogService.getProductById(productId);

        return new ResponseEntity<>(productCatalogResponse, HttpStatus.OK);
    }
}
