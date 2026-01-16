# Quick Testing Steps - Product Store Backend

## Step-by-Step Testing Guide

### ✅ STEP 1: Verify Backend is Running

1. **Start Spring Boot Application**
   - Make sure MySQL is running
   - Run your Spring Boot application
   - Check console for: `Started BackendStoreBackendApplication`
   - Verify port: `Tomcat started on port(s): 8081`

2. **Check Database Tables**
   - Open MySQL Workbench or command line
   - Run: `USE productdb;`
   - Run: `SHOW TABLES;`
   - You should see:
     ```
     - products
     - reviews
     - carts
     - cart_items
     ```

---

### ✅ STEP 2: Test Products (Basic CRUD)

#### 2.1 Create Test Products
Open Postman and run these one by one:

**Request 1: Create Dell Laptop**
```
POST http://localhost:8081/api/products
Content-Type: application/json

{
  "name": "Dell XPS 15",
  "brand": "Dell",
  "price": 1499.99,
  "category": "Laptops",
  "description": "High-performance laptop with 16GB RAM",
  "available": true,
  "quantity": 10
}
```
✅ **Expected:** Status 201, returns product with id=1

**Request 2: Create HP Laptop**
```
POST http://localhost:8081/api/products
Content-Type: application/json

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
✅ **Expected:** Status 201, returns product with id=2

**Request 3: Create MacBook (Out of Stock)**
```
POST http://localhost:8081/api/products
Content-Type: application/json

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
✅ **Expected:** Status 201, returns product with id=3

**Request 4: Create Samsung Phone**
```
POST http://localhost:8081/api/products
Content-Type: application/json

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
✅ **Expected:** Status 201, returns product with id=4

#### 2.2 Get All Products
```
GET http://localhost:8081/api/products
```
✅ **Expected:** Array with 4 products

---

### ✅ STEP 3: Test Search & Filter

#### 3.1 Search by Name
```
GET http://localhost:8081/api/products/search/name?name=laptop
```
✅ **Expected:** Returns 3 products (Dell, HP, MacBook)

#### 3.2 Search by Brand
```
GET http://localhost:8081/api/products/search/brand?brand=Dell
```
✅ **Expected:** Returns 1 product (Dell XPS 15)

#### 3.3 Filter by Availability
```
GET http://localhost:8081/api/products/search/available?available=true
```
✅ **Expected:** Returns 3 products (excludes MacBook)

#### 3.4 Filter by Price Range
```
GET http://localhost:8081/api/products/search/price?minPrice=500&maxPrice=1500
```
✅ **Expected:** Returns 3 products (excludes Samsung at 799 and Apple at 2499... wait, includes Samsung!)

#### 3.5 Advanced Search
```
GET http://localhost:8081/api/products/search?category=Laptops&minPrice=800&maxPrice=1600&available=true&page=0&size=10&sortBy=price&direction=asc
```
✅ **Expected:** Paginated response with 2 products (HP and Dell), sorted by price

---

### ✅ STEP 4: Test Pagination

#### 4.1 First Page (2 items per page)
```
GET http://localhost:8081/api/products/paginated?page=0&size=2&sortBy=price&direction=asc
```
✅ **Expected:**
- Returns 2 products (Samsung and HP - cheapest)
- totalElements: 4
- totalPages: 2
- number: 0

#### 4.2 Second Page
```
GET http://localhost:8081/api/products/paginated?page=1&size=2&sortBy=price&direction=asc
```
✅ **Expected:**
- Returns 2 products (Dell and Apple)
- number: 1

#### 4.3 Sort by Name
```
GET http://localhost:8081/api/products/paginated?page=0&size=10&sortBy=name&direction=asc
```
✅ **Expected:** Products sorted alphabetically (Apple, Dell, HP, Samsung)

---

### ✅ STEP 5: Test Reviews

#### 5.1 Add First Review to Product 1 (Dell)
```
POST http://localhost:8081/api/reviews/product/1
Content-Type: application/json

{
  "reviewerName": "Alice Johnson",
  "rating": 5,
  "comment": "Amazing laptop! Best purchase ever. The performance is incredible."
}
```
✅ **Expected:** Status 201, returns review with id=1

#### 5.2 Add Second Review to Product 1
```
POST http://localhost:8081/api/reviews/product/1
Content-Type: application/json

{
  "reviewerName": "Bob Smith",
  "rating": 4,
  "comment": "Good laptop, slightly expensive but worth it."
}
```
✅ **Expected:** Status 201, returns review with id=2

#### 5.3 Add Third Review to Product 1
```
POST http://localhost:8081/api/reviews/product/1
Content-Type: application/json

{
  "reviewerName": "Charlie Brown",
  "rating": 5,
  "comment": "Excellent build quality and fast performance!"
}
```
✅ **Expected:** Status 201, returns review with id=3

#### 5.4 Get All Reviews for Product 1
```
GET http://localhost:8081/api/reviews/product/1
```
✅ **Expected:** Array with 3 reviews

#### 5.5 Get Review Statistics
```
GET http://localhost:8081/api/reviews/product/1/stats
```
✅ **Expected:**
```json
{
  "averageRating": 4.7,
  "reviewCount": 3
}
```

#### 5.6 Get Only 5-Star Reviews
```
GET http://localhost:8081/api/reviews/product/1/rating/5
```
✅ **Expected:** 2 reviews (Alice and Charlie)

#### 5.7 Get Reviews with Minimum 4 Stars
```
GET http://localhost:8081/api/reviews/product/1/min-rating?minRating=4
```
✅ **Expected:** All 3 reviews

---

### ✅ STEP 6: Test Shopping Cart

#### 6.1 Add Dell Laptop to Cart (User: john123)
```
POST http://localhost:8081/api/cart/john123/add
Content-Type: application/json

{
  "productId": 1,
  "quantity": 2
}
```
✅ **Expected:** Cart created with 1 item (2 Dell laptops)

#### 6.2 Add HP Laptop to Same Cart
```
POST http://localhost:8081/api/cart/john123/add
Content-Type: application/json

{
  "productId": 2,
  "quantity": 1
}
```
✅ **Expected:** Cart now has 2 items

#### 6.3 Add Samsung Phone to Cart
```
POST http://localhost:8081/api/cart/john123/add
Content-Type: application/json

{
  "productId": 4,
  "quantity": 3
}
```
✅ **Expected:** Cart now has 3 items

#### 6.4 Get Cart
```
GET http://localhost:8081/api/cart/john123
```
✅ **Expected:**
- Cart with 3 items
- cartItems array showing all products
- Each item shows product details, quantity, and price

#### 6.5 Get Cart Summary
```
GET http://localhost:8081/api/cart/john123/summary
```
✅ **Expected:**
```json
{
  "totalPrice": 6299.94,  // (2*1499.99) + (1*899.99) + (3*799.99)
  "totalItems": 6,         // 2 + 1 + 3
  "itemCount": 3           // 3 different products
}
```

#### 6.6 Update Dell Laptop Quantity
```
PUT http://localhost:8081/api/cart/john123/update
Content-Type: application/json

{
  "productId": 1,
  "quantity": 5
}
```
✅ **Expected:** Dell quantity changed from 2 to 5

#### 6.7 Get Updated Summary
```
GET http://localhost:8081/api/cart/john123/summary
```
✅ **Expected:**
- totalPrice increased (now 5*1499.99 instead of 2*1499.99)
- totalItems: 9 (5 + 1 + 3)

#### 6.8 Remove HP Laptop from Cart
```
DELETE http://localhost:8081/api/cart/john123/remove/2
```
✅ **Expected:** Cart now has 2 items (Dell and Samsung)

#### 6.9 Add Dell Again (Should Increase Quantity)
```
POST http://localhost:8081/api/cart/john123/add
Content-Type: application/json

{
  "productId": 1,
  "quantity": 2
}
```
✅ **Expected:** Dell quantity increases from 5 to 7 (existing + new)

#### 6.10 Clear Cart
```
DELETE http://localhost:8081/api/cart/john123/clear
```
✅ **Expected:** Cart still exists but cartItems is empty

---

### ✅ STEP 7: Test Multiple Users

#### 7.1 Create Cart for User 2
```
POST http://localhost:8081/api/cart/mary456/add
Content-Type: application/json

{
  "productId": 3,
  "quantity": 1
}
```
✅ **Expected:** New cart created for mary456

#### 7.2 Verify Both Carts Exist Independently
```
GET http://localhost:8081/api/cart/john123
GET http://localhost:8081/api/cart/mary456
```
✅ **Expected:** Two separate carts with different contents

---

## Common Verification Checks

### ✅ Database Verification (MySQL)
```sql
-- Check products
SELECT * FROM products;

-- Check reviews
SELECT * FROM reviews;

-- Check carts
SELECT * FROM carts;

-- Check cart items with product details
SELECT ci.*, p.name, p.price
FROM cart_items ci
JOIN products p ON ci.product_id = p.id;

-- Get cart with totals
SELECT c.user_id,
       COUNT(ci.id) as item_count,
       SUM(ci.quantity) as total_items,
       SUM(ci.quantity * ci.price) as total_price
FROM carts c
LEFT JOIN cart_items ci ON c.id = ci.cart_id
GROUP BY c.id, c.user_id;
```

---

## Troubleshooting

### Issue: 404 Not Found
- ✅ Check Spring Boot is running
- ✅ Verify URL: http://localhost:8081 (not 8080)
- ✅ Check endpoint path is correct

### Issue: 500 Internal Server Error
- ✅ Check MySQL is running
- ✅ Check database 'productdb' exists
- ✅ Check application.properties is correct
- ✅ Look at console logs for details

### Issue: Product not found when adding review
- ✅ Create products first
- ✅ Use correct product ID from creation response

### Issue: Foreign key constraint error
- ✅ Restart Spring Boot with updated application.properties
- ✅ Check tables are using InnoDB: `SHOW CREATE TABLE reviews;`

### Issue: Cart items not showing product details
- ✅ This is expected if using FetchType.LAZY
- ✅ Product details are embedded in the response

---

## Success Criteria ✅

You should be able to:
- ✅ Create, read, update, delete products
- ✅ Search products by name, brand, category
- ✅ Filter products by price range and availability
- ✅ Get paginated results with sorting
- ✅ Add reviews to products (1-5 stars)
- ✅ Calculate average ratings
- ✅ Create shopping carts per user
- ✅ Add/remove/update cart items
- ✅ Get cart totals
- ✅ Multiple users have separate carts

If all tests pass, your backend is ready! 🎉
