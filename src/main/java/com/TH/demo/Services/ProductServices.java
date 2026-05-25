package com.TH.demo.Services;

import com.TH.demo.Model.Category;
import com.TH.demo.Model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface ProductServices {
    public Page<Product> getAllProduct(Pageable pageable);
    public Product getProductById(Long id);
    public void saveProduct(Product product);
    public void deleteProductById(Long id);
    public Page<Product> searchProducts(String keyword, Pageable pageable);
    public Page<Product> getProductsByCategory(Category category, Pageable pageable);
    public Product getProductByOrderDetailId(Long orderDetailId);
    }

