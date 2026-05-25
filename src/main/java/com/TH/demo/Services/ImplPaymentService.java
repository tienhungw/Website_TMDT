package com.TH.demo.Services;

import com.TH.demo.Model.CartItem;
import com.TH.demo.Model.Order;
import com.TH.demo.Model.OrderDetail;
import com.TH.demo.Model.OrderStatus;
import com.TH.demo.Model.Product;
import com.TH.demo.Repositories.CartItemRepository;
import com.TH.demo.Repositories.OrderRepository;
import com.TH.demo.Repositories.OrderdetailRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ImplPaymentService implements PaymentService {
    private static final Logger log = LoggerFactory.getLogger(ImplPaymentService.class);

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderdetailRepository orderdetailRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ProductServices productServices;

    @Override
    @Transactional
    public boolean confirmPayment(Long orderId, String username) {
        Optional<Order> opt = orderRepository.findById(orderId);
        if (opt.isEmpty()) {
            log.warn("[Xác nhận thanh toán: không tìm thấy đơn hàng [{}]]", orderId);
            return false;
        }
        Order order = opt.get();

        if (!order.getUser().getUsername().equals(username)) {
            log.warn("[Xác nhận thanh toán: user [{}] không sở hữu đơn [{}]]", username, orderId);
            return false;
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            log.info("[Xác nhận thanh toán: đơn [{}] không ở trạng thái PENDING (đang [{}]), bỏ qua]",
                    orderId, order.getStatus());
            return false;
        }

        List<CartItem> cartItems = cartItemRepository.findByUser(order.getUser());
        if (cartItems.isEmpty()) {
            log.warn("[Xác nhận thanh toán: giỏ hàng trống cho đơn [{}]]", orderId);
            return false;
        }

        for (CartItem item : cartItems) {
            Product product = productServices.getProductById(item.getProduct().getId());
            if (product.getQuantity() < item.getQuantity()) {
                log.warn("[Xác nhận thanh toán: sản phẩm '{}' không đủ hàng cho đơn [{}]]",
                        product.getName(), orderId);
                return false;
            }
        }

        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() - item.getQuantity());
            productServices.saveProduct(product);

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);
            orderDetail.setProduct(product);
            orderDetail.setQuantity(item.getQuantity());
            orderDetail.setPrice(product.getPrice());
            orderdetailRepository.save(orderDetail);
        }

        cartItemRepository.deleteAll(cartItems);

        order.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);

        log.info("[Xác nhận thanh toán thành công cho đơn [{}] của user [{}]]", orderId, username);
        return true;
    }
}
