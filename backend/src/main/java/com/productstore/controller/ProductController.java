package com.productstore.backend.controller;

import com.productstore.backend.model.Product;
import com.productstore.backend.service.ProductService;
import org.springframework.beans.factory.annotation

        .Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:3000")
public class ProductController {

    @Autowired
    private ProductService productService;

    // GET all products (simple list)
    // URL: http://localhost:8081/api/products
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    // GET all products with pagination and sorting
    // URL: http://localhost:8081/api/products/paginated?page=0&size=10&sortBy=name&direction=asc
    @GetMapping("/paginated")
    public ResponseEntity<Page<Product>> getAllProductsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Page<Product> products = productService.getAllProductsPaginated(page, size, sortBy, direction);
        return ResponseEntity.ok(products);
    }

    // GET products with advanced search, filters, pagination and sorting
    // URL: http://localhost:8081/api/products/search?name=laptop&brand=Dell&category=Electronics&minPrice=100&maxPrice=2000&available=true&page=0&size=10&sortBy=price&direction=asc
    @GetMapping("/search")
    public ResponseEntity<Page<Product>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean available,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Page<Product> products = productService.searchProducts(
                name, brand, category, minPrice, maxPrice, available,
                page, size, sortBy, direction);
        return ResponseEntity.ok(products);
    }

    // GET products by name search
    // URL: http://localhost:8081/api/products/search/name?name=laptop
    @GetMapping("/search/name")
    public ResponseEntity<List<Product>> searchByName(@RequestParam String name) {
        List<Product> products = productService.searchByName(name);
        return ResponseEntity.ok(products);
    }

    // GET products by brand
    // URL: http://localhost:8081/api/products/search/brand?brand=Dell
    @GetMapping("/search/brand")
    public ResponseEntity<List<Product>> searchByBrand(@RequestParam String brand) {
        List<Product> products = productService.searchByBrand(brand);
        return ResponseEntity.ok(products);
    }

    // GET products by category
    // URL: http://localhost:8081/api/products/search/category?category=Electronics
    @GetMapping("/search/category")
    public ResponseEntity<List<Product>> searchByCategory(@RequestParam String category) {
        List<Product> products = productService.searchByCategory(category);
        return ResponseEntity.ok(products);
    }

    // GET products by availability
    // URL: http://localhost:8081/api/products/search/available?available=true
    @GetMapping("/search/available")
    public ResponseEntity<List<Product>> findByAvailability(@RequestParam Boolean available) {
        List<Product> products = productService.findByAvailability(available);
        return ResponseEntity.ok(products);
    }

    // GET products by price range
    // URL: http://localhost:8081/api/products/search/price?minPrice=100&maxPrice=2000
    @GetMapping("/search/price")
    public ResponseEntity<List<Product>> findByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        List<Product> products = productService.findByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    // GET product by ID
    // URL: http://localhost:8081/api/products/1
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(product -> ResponseEntity.ok().body(product))
                .orElse(ResponseEntity.notFound().build());
    }

    // POST - Add new product
    // URL: http://localhost:8081/api/products
    @PostMapping
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        Product savedProduct = productService.addProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    // PUT - Update product
    // URL: http://localhost:8081/api/products/1
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        try {
            Product updatedProduct = productService.updateProduct(id, productDetails);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE - Delete product
    // URL: http://localhost:8081/api/products/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}