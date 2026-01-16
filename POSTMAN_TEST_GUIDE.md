# Postman Testing Guide - Product Store Backend

Base URL: `http://localhost:8081`

## Prerequisites
1. Start your Spring Boot application
2. MySQL database should be running
3. Tables should be created automatically

---

## 1. PRODUCT ENDPOINTS

### 1.1 Get All Products (Simple)
```
GET http://localhost:8081/api/products
```

### 1.2 Get Products with Pagination
```
GET http://localhost:8081/api/products/paginated?page=0&size=5&sortBy=price&direction=asc
```

### 1.3 Search Products (Advanced)
```
GET http://localhost:8081/api/products/search?name=laptop&minPrice=500&maxPrice=2000&available=true&page=0&size=10&sortBy=price&direction=asc
```

### 1.4 Search by Name Only
```
GET http://localhost:8081/api/products/search/name?name=laptop
```

### 1.5 Search by Brand
```
GET http://localhost:8081/api/products/search/brand?brand=Dell
```

### 1.6 Search by Category
```
GET http://localhost:8081/api/products/search/category?category=Electronics
```

### 1.7 Search by Availability
```
GET http://localhost:8081/api/products/search/available?available=true
```

### 1.8 Search by Price Range
```
GET http://localhost:8081/api/products/search/price?minPrice=100&maxPrice=1000
```

### 1.9 Get Product by ID
```
GET http://localhost:8081/api/products/1
```

### 1.10 Create New Product
```
POST http://localhost:8081/api/products
Content-Type: application/json

{
  "name": "Dell XPS 15",
  "brand": "Dell",
  "price": 1499.99,
  "category": "Laptops",
  "description": "Powerful laptop for professionals",
  "available": true,
  "quantity": 10
}
```

### 1.11 Update Product
```
PUT http://localhost:8081/api/products/1
Content-Type: application/json

{
  "name": "Dell XPS 15 Updated",
  "brand": "Dell",
  "price": 1399.99,
  "category": "Laptops",
  "description": "Powerful laptop for professionals - Now on sale!",
  "available": true,
  "quantity": 15
}
```

### 1.12 Delete Product
```
DELETE http://localhost:8081/api/products/1
```

---

## 2. REVIEW ENDPOINTS

### 2.1 Get All Reviews
```
GET http://localhost:8081/api/reviews
```

### 2.2 Get Review by ID
```
GET http://localhost:8081/api/reviews/1
```

### 2.3 Get All Reviews for a Product
```
GET http://localhost:8081/api/reviews/product/1
```

### 2.4 Get Reviews by Product and Rating
```
GET http://localhost:8081/api/reviews/product/1/rating/5
```

### 2.5 Get Reviews with Minimum Rating
```
GET http://localhost:8081/api/reviews/product/1/min-rating?minRating=4
```

### 2.6 Get Product Review Statistics
```
GET http://localhost:8081/api/reviews/product/1/stats
```

### 2.7 Add Review to Product
```
POST http://localhost:8081/api/reviews/product/1
Content-Type: application/json

{
  "reviewerName": "John Doe",
  "rating": 5,
  "comment": "Excellent product! Highly recommended."
}
```

### 2.8 Update Review
```
PUT http://localhost:8081/api/reviews/1
Content-Type: application/json

{
  "reviewerName": "John Doe",
  "rating": 4,
  "comment": "Great product, but a bit expensive."
}
```

### 2.9 Delete Review
```
DELETE http://localhost:8081/api/reviews/1
```

---

## 3. CART ENDPOINTS

### 3.1 Get Cart for User
```
GET http://localhost:8081/api/cart/user123
```

### 3.2 Get Cart Summary
```
GET http://localhost:8081/api/cart/user123/summary
```

### 3.3 Add Item to Cart
```
POST http://localhost:8081/api/cart/user123/add
Content-Type: application/json

{
  "productId": 1,
  "quantity": 2
}
```

### 3.4 Update Cart Item Quantity
```
PUT http://localhost:8081/api/cart/user123/update
Content-Type: application/json

{
  "productId": 1,
  "quantity": 5
}
```

### 3.5 Remove Item from Cart
```
DELETE http://localhost:8081/api/cart/user123/remove/1
```

### 3.6 Clear Cart
```
DELETE http://localhost:8081/api/cart/user123/clear
```

### 3.7 Delete Cart
```
DELETE http://localhost:8081/api/cart/user123
```

---

## TESTING WORKFLOW

### Step 1: Create Test Products
Create 3-5 products with different brands, categories, and prices:

**Product 1:**
```json
POST http://localhost:8081/api/products
{
  "name": "Dell XPS 15",
  "brand": "Dell",
  "price": 1499.99,
  "category": "Laptops",
  "description": "High-performance laptop",
  "available": true,
  "quantity": 10
}
```

**Product 2:**
```json
POST http://localhost:8081/api/products
{
  "name": "HP Pavilion",
  "brand": "HP",
  "price": 899.99,
  "category": "Laptops",
  "description": "Budget-friendly laptop",
  "available": true,
  "quantity": 15
}
```

**Product 3:**
```json
POST http://localhost:8081/api/products
{
  "name": "Apple MacBook Pro",
  "brand": "Apple",
  "price": 2499.99,
  "category": "Laptops",
  "description": "Premium laptop for professionals",
  "available": false,
  "quantity": 0
}
```

**Product 4:**
```json
POST http://localhost:8081/api/products
{
  "name": "Samsung Galaxy S23",
  "brand": "Samsung",
  "price": 799.99,
  "category": "Smartphones",
  "description": "Latest flagship smartphone",
  "available": true,
  "quantity": 20
}
```

### Step 2: Test Search & Filter
1. Search by name: `GET /api/products/search/name?name=laptop`
2. Search by brand: `GET /api/products/search/brand?brand=Dell`
3. Filter by price: `GET /api/products/search/price?minPrice=500&maxPrice=1500`
4. Filter available only: `GET /api/products/search/available?available=true`
5. Advanced search: `GET /api/products/search?category=Laptops&minPrice=800&maxPrice=1600&available=true`

### Step 3: Test Pagination
1. First page: `GET /api/products/paginated?page=0&size=2&sortBy=price&direction=asc`
2. Second page: `GET /api/products/paginated?page=1&size=2&sortBy=price&direction=asc`
3. Sort by name: `GET /api/products/paginated?page=0&size=10&sortBy=name&direction=asc`

### Step 4: Test Reviews
1. Add review to product 1:
```json
POST http://localhost:8081/api/reviews/product/1
{
  "reviewerName": "Alice Johnson",
  "rating": 5,
  "comment": "Amazing laptop! Best purchase ever."
}
```

2. Add more reviews:
```json
POST http://localhost:8081/api/reviews/product/1
{
  "reviewerName": "Bob Smith",
  "rating": 4,
  "comment": "Good product, slightly expensive."
}
```

3. Get all reviews: `GET /api/reviews/product/1`
4. Get statistics: `GET /api/reviews/product/1/stats`
5. Filter 5-star reviews: `GET /api/reviews/product/1/rating/5`

### Step 5: Test Shopping Cart
1. Create cart and add item:
```json
POST http://localhost:8081/api/cart/user123/add
{
  "productId": 1,
  "quantity": 2
}
```

2. Add another product:
```json
POST http://localhost:8081/api/cart/user123/add
{
  "productId": 2,
  "quantity": 1
}
```

3. Get cart: `GET /api/cart/user123`
4. Get summary: `GET /api/cart/user123/summary`
5. Update quantity:
```json
PUT http://localhost:8081/api/cart/user123/update
{
  "productId": 1,
  "quantity": 5
}
```

6. Remove item: `DELETE /api/cart/user123/remove/2`
7. Clear cart: `DELETE /api/cart/user123/clear`

---

## Expected Responses

### Successful Product Creation (201 Created)
```json
{
  "id": 1,
  "name": "Dell XPS 15",
  "brand": "Dell",
  "price": 1499.99,
  "category": "Laptops",
  "description": "High-performance laptop",
  "available": true,
  "quantity": 10
}
```

### Paginated Response
```json
{
  "content": [...products...],
  "pageable": {...},
  "totalPages": 2,
  "totalElements": 10,
  "size": 5,
  "number": 0
}
```

### Review Stats Response
```json
{
  "averageRating": 4.5,
  "reviewCount": 2
}
```

### Cart Summary Response
```json
{
  "totalPrice": 2999.98,
  "totalItems": 3,
  "itemCount": 2
}
```

---

## Common Issues & Solutions

### Issue: 404 Not Found
- **Solution:** Make sure Spring Boot application is running on port 8081

### Issue: 500 Internal Server Error
- **Solution:** Check if MySQL is running and database 'productdb' exists

### Issue: Foreign Key Constraint Error
- **Solution:** Make sure the updated application.properties is loaded (MySQL8Dialect with InnoDB)

### Issue: Product Not Found when adding review
- **Solution:** Create products first before adding reviews

### Issue: Tables not created
- **Solution:** Restart Spring Boot application after updating application.properties

---

## Testing Checklist

- [ ] Create at least 3 products
- [ ] Get all products
- [ ] Test pagination (page 0, page 1)
- [ ] Test sorting (by price, by name)
- [ ] Search by name
- [ ] Search by brand
- [ ] Search by category
- [ ] Filter by price range
- [ ] Filter by availability
- [ ] Advanced multi-filter search
- [ ] Add 2-3 reviews to a product
- [ ] Get reviews for product
- [ ] Get review statistics
- [ ] Update a review
- [ ] Create cart and add items
- [ ] Update cart item quantity
- [ ] Get cart summary
- [ ] Remove item from cart
- [ ] Clear cart

---

## Notes
- Replace `user123` with any string (userId, email, session ID)
- Product IDs will auto-increment starting from 1
- Review ratings must be between 1-5
- Quantity in cart must be positive integer
- All prices are in BigDecimal format

Good luck with testing! 🚀
