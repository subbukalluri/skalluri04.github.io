import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import ProductService from '../services/ProductService';
import './ProductForm.css';

function AddProduct() {
    const [product, setProduct] = useState({
        name: '',
        brand: '',
        price: '',
        category: '',
        description: '',
        available: true,
        quantity: ''
    });

    const navigate = useNavigate();

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setProduct({
            ...product,
            [name]: type === 'checkbox' ? checked : value
        });
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        
        // Validation
        if (!product.name || !product.brand || !product.price || !product.quantity) {
            alert('Please fill in all required fields!');
            return;
        }

        // Convert price and quantity to numbers
        const productData = {
            ...product,
            price: parseFloat(product.price),
            quantity: parseInt(product.quantity)
        };

        ProductService.createProduct(productData)
            .then(() => {
                alert('Product added successfully!');
                navigate('/');
            })
            .catch(error => {
                console.error('Error adding product:', error);
                alert('Error adding product!');
            });
    };

    return (
        <div className="form-container">
            <div className="form-header">
                <h1>Add New Product</h1>
                <button className="btn-back" onClick={() => navigate('/')}>
                    Back to List
                </button>
            </div>

            <form onSubmit={handleSubmit} className="product-form">
                <div className="form-group">
                    <label>Product Name *</label>
                    <input
                        type="text"
                        name="name"
                        value={product.name}
                        onChange={handleChange}
                        placeholder="Enter product name"
                        required
                    />
                </div>

                <div className="form-group">
                    <label>Brand *</label>
                    <input
                        type="text"
                        name="brand"
                        value={product.brand}
                        onChange={handleChange}
                        placeholder="Enter brand name"
                        required
                    />
                </div>

                <div className="form-row">
                    <div className="form-group">
                        <label>Price *</label>
                        <input
                            type="number"
                            name="price"
                            value={product.price}
                            onChange={handleChange}
                            placeholder="0.00"
                            step="0.01"
                            min="0"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label>Quantity *</label>
                        <input
                            type="number"
                            name="quantity"
                            value={product.quantity}
                            onChange={handleChange}
                            placeholder="0"
                            min="0"
                            required
                        />
                    </div>
                </div>

                <div className="form-group">
                    <label>Category</label>
                    <input
                        type="text"
                        name="category"
                        value={product.category}
                        onChange={handleChange}
                        placeholder="Enter category (e.g., Electronics)"
                    />
                </div>

                <div className="form-group">
                    <label>Description</label>
                    <textarea
                        name="description"
                        value={product.description}
                        onChange={handleChange}
                        placeholder="Enter product description"
                        rows="4"
                    />
                </div>

                <div className="form-group checkbox-group">
                    <label>
                        <input
                            type="checkbox"
                            name="available"
                            checked={product.available}
                            onChange={handleChange}
                        />
                        Available in Stock
                    </label>
                </div>

                <div className="form-actions">
                    <button type="submit" className="btn-submit">
                        Add Product
                    </button>
                    <button 
                        type="button" 
                        className="btn-cancel"
                        onClick={() => navigate('/')}
                    >
                        Cancel
                    </button>
                </div>
            </form>
        </div>
    );
}

export default AddProduct;