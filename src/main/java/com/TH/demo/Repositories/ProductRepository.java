package com.TH.demo.Repositories;

import com.TH.demo.Model.Category;
import com.TH.demo.Model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByCategory(Category category, Pageable pageable);
    Page<Product> findByNormalizedNameContainingIgnoreCase(String keyword, Pageable pageable);
}
