package org.shvk.productcatalogservice.controller;

import org.shvk.productcatalogservice.model.ProductCatalogRequest;
import org.shvk.productcatalogservice.service.ProductCatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
