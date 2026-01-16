package com.productstore.backend.repository;

import com.productstore.backend.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Search products by name (case-insensitive)
    List<Product> findByNameContainingIgnoreCase(String name);

    // Search products by brand (case-insensitive)
    List<Product> findByBrandContainingIgnoreCase(String brand);

    // Search products by category (case-insensitive)
    List<Product> findByCategoryContainingIgnoreCase(String category);

    // Find products by availability
    List<Product> findByAvailable(Boolean available);

    // Find products within price range
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // Advanced search with multiple criteria
    @Query("SELECT p FROM Product p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:brand IS NULL OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :brand, '%'))) AND " +
           "(:category IS NULL OR LOWER(p.category) LIKE LOWER(CONCAT('%', :category, '%'))) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:available IS NULL OR p.available = :available)")
    Page<Product> searchProducts(
        @Param("name") String name,
        @Param("brand") String brand,
        @Param("category") String category,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("available") Boolean available,
        Pageable pageable
    );

    // Get all products with pagination
    Page<Product> findAll(Pageable pageable);
}