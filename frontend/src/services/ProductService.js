import axios from 'axios';

const API_BASE_URL = "http://localhost:8081/api/products";

class ProductService {
    // Get all products
    getAllProducts() {
        return axios.get(API_BASE_URL);
    }

    // Get product by ID
    getProductById(id) {
        return axios.get(`${API_BASE_URL}/${id}`);
    }

    // Create new product
    createProduct(product) {
        return axios.post(API_BASE_URL, product);
    }

    // Update product
    updateProduct(id, product) {
        return axios.put(`${API_BASE_URL}/${id}`, product);
    }

    // Delete product
    deleteProduct(id) {
        return axios.delete(`${API_BASE_URL}/${id}`);
    }
}

export default new ProductService();