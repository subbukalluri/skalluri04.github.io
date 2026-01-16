import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import ProductService from '../services/ProductService';
import './ProductList.css';

function ProductList() {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        loadProducts();
    }, []);

    const loadProducts = () => {
        ProductService.getAllProducts()
            .then(response => {
                setProducts(response.data);
                setLoading(false);
            })
            .catch(error => {
                console.error("Error loading products:", error);
                setLoading(false);
            });
    };

    const deleteProduct = (id) => {
        if (window.confirm('Are you sure you want to delete this product?')) {
            ProductService.deleteProduct(id)
                .then(() => {
                    loadProducts(); // Reload the list
                    alert('Product deleted successfully!');
                })
                .catch(error => {
                    console.error("Error deleting product:", error);
                    alert('Error deleting product!');
                });
        }
    };

    if (loading) {
        return <div className="loading">Loading products...</div>;
    }

    return (
        <div className="product-list-container">
            <div className="header">
                <h1>Product Store</h1>
                <button 
                    className="btn-add" 
                    onClick={() => navigate('/add')}
                >
                    Add New Product
                </button>
            </div>

            {products.length === 0 ? (
                <div className="empty-state">
                    <p>No products available. Add your first product!</p>
                </div>
            ) : (
                <div className="product-grid">
                    {products.map(product => (
                        <div key={product.id} className="product-card">
                            <div className="product-header">
                                <h3>{product.name}</h3>
                                <span className={`badge ${product.available ? 'available' : 'unavailable'}`}>
                                    {product.available ? 'In Stock' : 'Out of Stock'}
                                </span>
                            </div>
                            <div className="product-body">
                                <p className="brand">{product.brand}</p>
                                <p className="category">{product.category}</p>
                                <p className="description">{product.description}</p>
                                <p className="price">${product.price}</p>
                                <p className="quantity">Quantity: {product.quantity}</p>
                            </div>
                            <div className="product-actions">
                                <button 
                                    className="btn-edit"
                                    onClick={() => navigate(`/edit/${product.id}`)}
                                >
                                    Edit
                                </button>
                                <button 
                                    className="btn-delete"
                                    onClick={() => deleteProduct(product.id)}
                                >
                                    Delete
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}

export default ProductList;