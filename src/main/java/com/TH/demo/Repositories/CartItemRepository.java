package com.TH.demo.Repositories;

import com.TH.demo.Model.CartItem;
import com.TH.demo.Model.Product;
import com.TH.demo.Model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByUserAndProduct(Users user, Product product);
    List<CartItem> findByUser(Users user);
}
