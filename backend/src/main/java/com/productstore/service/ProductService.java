package com.productstore.backend.service;

import com.productstore.backend.model.Product;
import com.productstore.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // Get all products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Get all products with pagination and sorting
    public Page<Product> getAllProductsPaginated(int page, int size, String sortBy, String direction) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return productRepository.findAll(pageable);
    }

    // Search products with filters and pagination
    public Page<Product> searchProducts(String name, String brand, String category,
                                       BigDecimal minPrice, BigDecimal maxPrice,
                                       Boolean available, int page, int size,
                                       String sortBy, String direction) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return productRepository.searchProducts(name, brand, category, minPrice, maxPrice, available, pageable);
    }

    // Search by name
    public List<Product> searchByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    // Search by brand
    public List<Product> searchByBrand(String brand) {
        return productRepository.findByBrandContainingIgnoreCase(brand);
    }

    // Search by category
    public List<Product> searchByCategory(String category) {
        return productRepository.findByCategoryContainingIgnoreCase(category);
    }

    // Find by availability
    public List<Product> findByAvailability(Boolean available) {
        return productRepository.findByAvailable(available);
    }

    // Find by price range
    public List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    // Get product by ID
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // Add new product
    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    // Update existing product
    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setName(productDetails.getName());
        product.setBrand(productDetails.getBrand());
        product.setPrice(productDetails.getPrice());
        product.setCategory(productDetails.getCategory());
        product.setDescription(productDetails.getDescription());
        product.setAvailable(productDetails.getAvailable());
        product.setQuantity(productDetails.getQuantity());

        return productRepository.save(product);
    }

    // Delete product
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        productRepository.delete(product);
    }
}