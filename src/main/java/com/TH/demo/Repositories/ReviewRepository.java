package com.TH.demo.Repositories;

import com.TH.demo.Model.Product;
import com.TH.demo.Model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProduct(Product product);
}
