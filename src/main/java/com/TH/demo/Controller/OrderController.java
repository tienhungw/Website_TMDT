package com.TH.demo.Controller;

import com.TH.demo.Model.*;
import com.TH.demo.Repositories.CartItemRepository;
import com.TH.demo.Repositories.OrderRepository;
import com.TH.demo.Repositories.OrderdetailRepository;
import com.TH.demo.Repositories.UserRepository;
import com.TH.demo.Services.CartServices;
import com.TH.demo.Services.OrderServices;
import com.TH.demo.Services.PaymentService;
import com.TH.demo.Services.ProductServices;
import org.aspectj.weaver.ast.Or;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
public class OrderController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    CartServices cartServices;
    @Autowired
    ProductServices productServices;
    @Autowired
    OrderServices orderServices;
    @Autowired
    OrderdetailRepository orderdetailRepository;
    @Autowired
    CartItemRepository cartItemRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    PaymentService paymentService;

    @Value("${bank.code}")
    private String bankCode;
    @Value("${bank.account}")
    private String bankAccount;
    @Value("${bank.accountName}")
    private String bankAccountName;

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    @GetMapping("/user/order")
    public String TrangDatHang(Model model, Principal principal){
        String username = principal.getName();
        Users user = userRepository.findByUsername(username).orElseThrow();
        model.addAttribute("user", user);

        List<CartItem> cartItems = cartServices.getCartItemsByUser(user);
        if (cartItems.isEmpty()) {
            return "redirect:/user/cart";
        }

        model.addAttribute("cartItems", cartItems);

        double total = cartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        model.addAttribute("totalAmount", total);
        log.info("[USER [{}] truy cập vào trang order]", username);

        return "dathang";
    }
//    @PostMapping("/user/order/checkout")
//    public String checkout(Principal principal, RedirectAttributes redirectAttributes) {
//        String username = principal.getName();
//        Users user = userRepository.findByUsername(username).orElseThrow();
////        if (user.getAddress() == null || user.getAddress().trim().isEmpty()){
////            redirectAttributes.addFlashAttribute("errorAddress", "Bạn cần cập nhật địa chỉ");
////            return "redirect:/user/me";
////        }
//        List<CartItem> cartItems = cartServices.getCartItemsByUser(user);
//        for (CartItem item : cartItems) {
//            Product product = productServices.getProductById(item.getProduct().getId());
//            if (product.getQuantity() < item.getQuantity()) {
//                redirectAttributes.addFlashAttribute("error",
//                        "Sản phẩm '" + product.getName() + "' không đủ hàng. Chỉ còn " + product.getQuantity() + " sản phẩm.");
//                return "redirect:/user/cart";
//            }
//        }
//        double total = cartItems.stream()
//                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
//                .sum();
//
//        Order order = new Order();
//        order.setUser(user);
//        order.setTotalPrice(total);
//        order.setStatus(OrderStatus.PROCESSING);
//        order.setOrderDate(LocalDate.now());
//
//        orderServices.saveOrder(order);
//        for(CartItem item : cartItems){
//            Product product = item.getProduct();
//            // Trừ số lượng sản phẩm
//            int newQuantity = product.getQuantity() - item.getQuantity();
//            if (newQuantity < 0) {
//                // Nếu không đủ hàng, có thể xử lý tùy bạn: bỏ qua đơn, báo lỗi, ...
//                throw new IllegalArgumentException("Sản phẩm " + product.getName() + " không đủ hàng.");
//            }
//            product.setQuantity(newQuantity);
//            productServices.saveProduct(product);
//            OrderDetail orderDetail = new OrderDetail();
//            orderDetail.setOrder(order);
//            orderDetail.setProduct(item.getProduct());
//            orderDetail.setQuantity(item.getQuantity());
//            orderDetail.setPrice(item.getProduct().getPrice());
//            orderdetailRepository.save(orderDetail);
//        }
//        cartItemRepository.deleteAll();
//        return "redirect:/user/order/success";
//    }

    @PostMapping("/user/order/checkout")
    public String checkout(Principal principal, RedirectAttributes redirectAttributes) {
        String username = principal.getName();
        Users user = userRepository.findByUsername(username).orElseThrow();
        log.info("[USER [{}] tiến hành checking]", username);

        List<CartItem> cartItems = cartServices.getCartItemsByUser(user);
        log.info("[USER [{}] có [{}] sản phẩm trong giỏ hàng]", username, cartItems.size());

        for (CartItem item : cartItems) {
            Product product = productServices.getProductById(item.getProduct().getId());
            if (product.getQuantity() < item.getQuantity()) {
                log.warn("USER [{}] tiến hành đặt [{}] sản phẩm '{}', nhưng chỉ còn [{}] sp trong kho]",
                        username, item.getQuantity(), product.getName(), product.getQuantity());
                redirectAttributes.addFlashAttribute("error",
                        "Sản phẩm '" + product.getName() + "' không đủ hàng. Chỉ còn " + product.getQuantity() + " sản phẩm.");
                return "redirect:/user/cart";
            }
        }

        double total = cartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
        log.info("USER [{}] có tổng giá trị đơn hàng {}]", username, total);

        Order order = new Order();
        order.setUser(user);
        order.setTotalPrice(total);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDate.now());

        orderServices.saveOrder(order);
        log.info("[Đơn hàng PENDING đã được tạo cho USER [{}] với ID [{}] và tổng giá trị là [{}], chờ thanh toán]",
                username, order.getId(), total);

        return "redirect:/user/order/" + order.getId() + "/pay";
    }

    @GetMapping("/user/order/{id}/pay")
    public String payPage(@PathVariable("id") Long orderId, Model model, Principal principal) {
        Optional<Order> opt = orderRepository.findById(orderId);
        if (opt.isEmpty()) {
            return "redirect:/user/cart";
        }
        Order order = opt.get();
        if (!order.getUser().getUsername().equals(principal.getName())) {
            log.warn("[USER [{}] cố truy cập trang thanh toán của đơn [{}] không phải của họ]",
                    principal.getName(), orderId);
            return "redirect:/user/cart";
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            return "redirect:/user/order/success";
        }

        long amount = Math.round(order.getTotalPrice());
        model.addAttribute("order", order);
        model.addAttribute("bankCode", bankCode);
        model.addAttribute("bankAccount", bankAccount);
        model.addAttribute("bankAccountName", bankAccountName);
        model.addAttribute("amount", amount);
        model.addAttribute("memo", "DH" + order.getId());
        log.info("[USER [{}] truy cập trang thanh toán QR cho đơn [{}], số tiền [{}]]",
                principal.getName(), orderId, amount);

        return "thanhtoan";
    }

    @PostMapping("/user/order/{id}/confirm-paid")
    public String confirmPaid(@PathVariable("id") Long orderId, Principal principal,
                              RedirectAttributes redirectAttributes) {
        boolean ok = paymentService.confirmPayment(orderId, principal.getName());
        if (ok) {
            return "redirect:/user/order/success";
        }
        redirectAttributes.addFlashAttribute("error",
                "Không thể xác nhận thanh toán. Vui lòng thử lại hoặc liên hệ admin.");
        return "redirect:/user/order/" + orderId + "/pay";
    }

    @GetMapping("/user/order/success")
    public String success(){
        return "okay";
    }

    @GetMapping("admin/order")
    public String trangDatHang(Model model){
        List<Order> orders = orderRepository.findAll();
        model.addAttribute("statusList", OrderStatus.values());
        model.addAttribute("orders", orders);
        log.info("[ADMIN truy cập trang danh sách orders]");

        return "admin-dondathang";
    }

    @PostMapping("/admin/order/status")
    public String updateStatusForOrder(@RequestParam("orderId") Long orderId,
                                    @RequestParam("status") OrderStatus status) {
        Order order = orderRepository.getReferenceById(orderId);
        if (order != null) {
            order.setStatus(status);
            orderRepository.save(order);
        }
        log.info("[ADMIN đã cập nhật cho đơn hàng [{}] với status [{}]", orderId, status);

        return "redirect:/admin/order";
    }

    @GetMapping("/admin/order/{id}")
    public String getDetailOrder(@PathVariable("id") Long orderId, Model model){
        Order order = orderRepository.getReferenceById(orderId);
        Users user = order.getUser();
        List<OrderDetail> orderDetails = orderdetailRepository.findByOrder(order);
        model.addAttribute("order", order);
        model.addAttribute("user", user);
        model.addAttribute("orderDetails", orderDetails);
        log.info("[ADMIN truy cập chi tiết đơn hàng: [{}]]", orderId);

        return "admin-hoadonchitiet";
    }
    @GetMapping("/user/order/{id}")
    public String getDetailOrderUser(@PathVariable("id") Long orderId, Model model, Principal principal){
        String username = principal.getName();
        Order order = orderRepository.getReferenceById(orderId);
        Users user = order.getUser();
        List<OrderDetail> orderDetails = orderdetailRepository.findByOrder(order);
        model.addAttribute("order", order);
        model.addAttribute("user", user);
        model.addAttribute("orderDetails", orderDetails);
        log.info("[USER [{}] truy cập chi tiết đơn hàng [{}]", username, orderId);

        return "hoadonchitiet";
    }
    @GetMapping("/user/history")
    public String historyOrder(Model model, Principal principal){
        String username = principal.getName();
        Users user = userRepository.findByUsername(username).orElseThrow();
        List<Order> orders = orderRepository.findByUser(user);
        model.addAttribute("orders", orders);
        log.info("[USER [{}] xem lịch sử mua hàng]", username);

        return "lichsu";
    }
}
