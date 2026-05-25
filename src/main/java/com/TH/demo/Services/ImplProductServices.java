package com.TH.demo.Services;

import com.TH.demo.Model.Category;
import com.TH.demo.Model.OrderDetail;
import com.TH.demo.Model.Product;
import com.TH.demo.Repositories.OrderdetailRepository;
import com.TH.demo.Repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;

@Service
public class ImplProductServices implements ProductServices{
    @Autowired
    ProductRepository productRepository;
    @Autowired
    OrderdetailRepository orderdetailRepository;

    @Override
    public Page<Product> getAllProduct(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Product getProductById(Long id){
        return productRepository.findById(id).orElse(null);
    }
    @Override
    public void saveProduct(Product product){
        productRepository.save(product);
    }
    @Override
    public void deleteProductById(Long id){
        productRepository.deleteById(id);
    }

    @Override
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        String normalizedKeyword = removeAccents(keyword);
        return productRepository.findByNormalizedNameContainingIgnoreCase(normalizedKeyword, pageable);
    }
    private String removeAccents(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }
    @Override
    public Page<Product> getProductsByCategory(Category category, Pageable pageable) {
        return productRepository.findByCategory(category, pageable);
    }
    @Override
    public Product getProductByOrderDetailId(Long orderDetailId) {
        Optional<OrderDetail> optional = orderdetailRepository.findById(orderDetailId);
        return optional.map(OrderDetail::getProduct).orElse(null);
    }
}
