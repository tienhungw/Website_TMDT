package com.TH.demo.Services;

import com.TH.demo.Model.CartItem;
import com.TH.demo.Model.Product;
import com.TH.demo.Model.Users;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface CartServices {
    public void addToCart(Users user, Product product, int quantity);
    public void updateQuantity(Long cartItemId, int quantity);
    public List<CartItem> getCartItemsByUser(Users user);
}
