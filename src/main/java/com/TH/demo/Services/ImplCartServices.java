package com.TH.demo.Services;

import com.TH.demo.Model.CartItem;
import com.TH.demo.Model.Product;
import com.TH.demo.Model.Users;
import com.TH.demo.Repositories.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ImplCartServices implements CartServices{
    @Autowired
    CartItemRepository cartItemRepository;

    @Override
    public void addToCart(Users user, Product product, int quantity) {
        // Kiểm tra tồn kho
        if (product.getQuantity() < quantity) {
            throw new IllegalArgumentException("Số lượng yêu cầu vượt quá số lượng tồn kho.");
        }
        // Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
        Optional<CartItem> cartItemOptional = cartItemRepository.findByUserAndProduct(user, product);

        if (cartItemOptional.isPresent()) {
            CartItem cartItem = cartItemOptional.get();
            int newQuantity = cartItem.getQuantity() + quantity;

            if (newQuantity > product.getQuantity()) {
                throw new IllegalArgumentException("Số lượng yêu cầu vượt quá số lượng tồn kho.");
            }

            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);

        } else {
            CartItem newCartItem = new CartItem();
            newCartItem.setUser(user);
            newCartItem.setProduct(product);
            newCartItem.setQuantity(quantity);
            cartItemRepository.save(newCartItem);
        }
    }
    @Override
    public void updateQuantity(Long cartItemId, int quantity) {
        Optional<CartItem> optional = cartItemRepository.findById(cartItemId);
        if (optional.isPresent()) {
            CartItem item = optional.get();
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
    }
    @Override
    public List<CartItem> getCartItemsByUser(Users user) {
        return cartItemRepository.findByUser(user);
    }
}
