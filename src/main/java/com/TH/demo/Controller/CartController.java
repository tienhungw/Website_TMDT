package com.TH.demo.Controller;

import com.TH.demo.Model.CartItem;
import com.TH.demo.Model.Product;
import com.TH.demo.Model.Users;
import com.TH.demo.Repositories.CartItemRepository;
import com.TH.demo.Repositories.UserRepository;
import com.TH.demo.Services.CartServices;
import com.TH.demo.Services.ProductServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
public class CartController {
    @Autowired
    CartItemRepository cartItemRepository;
    @Autowired
    CartServices cartServices;
    @Autowired
    ProductServices productServices;
    @Autowired
    UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    //Them san pham

    @PostMapping("/user/home/add")
    public String addToCart(@RequestParam("productId") Long productId,
                            @RequestParam(value = "quantity") int quantity,
                            RedirectAttributes redirectAttributes, Principal principal) {

        String username = principal.getName();
        Users user = userRepository.findByUsername(username).orElse(null);

        Product product = productServices.getProductById(productId);

        System.out.println(username);
        System.out.println(product.getName());
        System.out.println(quantity);

        try {
            cartServices.addToCart(user, product, quantity);
            redirectAttributes.addFlashAttribute("success", "Sản phẩm đã được thêm vào giỏ hàng.");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        log.info("[User [{}] đã thêm sản phẩm [{}] số lượng [{}] vào giỏ hàng]", user.getUsername(), product.getName(), quantity);

        return "redirect:/user/home";
    }

    @GetMapping("/user/cart")
    public String trangGioHang(Model model, Principal principal){
        String username = principal.getName();
        Users user = userRepository.findByUsername(username).orElse(null);

        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        double totalPrice = cartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
        System.out.println(totalPrice);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);
        log.info("[User [{}] đang xem giỏ hàng]", username);
        log.info("[Tổng tiền giỏ hàng của [{}]: {}]", username, totalPrice);

        return "giohang";
    }

    @PostMapping("/user/cart/update")
    public String updateCartItem(@RequestParam("cartItemId") Long cartItemId,
                                 @RequestParam("quantity") int quantity,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElse(null);
//        System.out.println(.getQuantity());
        String username = principal.getName();
        cartServices.updateQuantity(cartItemId, quantity);
//        System.out.println(cartItem.getQuantity());


        Product product = productServices.getProductById(cartItem.getProduct().getId());
        if (product.getQuantity() < cartItem.getQuantity()) {
            redirectAttributes.addFlashAttribute("error",
                    "Sản phẩm '" + product.getName() + "' không đủ hàng. Chỉ còn " + product.getQuantity() + " sản phẩm.");
            return "redirect:/user/cart";
        }
        log.info("[User [{}] đang cập nhật sản phẩm trong giỏ. product={}, quantity={}]", username, product.getName(), quantity);
        log.info("[Cập nhật giỏ hàng thành công cho sản phẩm [{}]]", product.getName());

        return "redirect:/user/cart";
    }
//    @GetMapping("/user/cart/delete/{id}")
//    public String deleteCartItem(@PathVariable(name= "id") Long id, RedirectAttributes redirectAttributes, Principal principal) {
//        String username = principal.getName();
//        CartItem cartItem = cartItemRepository.findById(id).orElse(null);
////        cartServices.deleteCartItemById(id);
//        cartItemRepository.deleteById(id);
//        log.info("[User [{}] đang xóa sản phẩm có cartItemId={}]", username, id);
//        log.info("[Đã xóa sản phẩm [{}] khỏi giỏ hàng của user [{}]]", cartItem.getProduct().getName(), username);
//
//        return "redirect:/user/cart";
//    }


    @GetMapping("/user/cart/delete/{id}")
    public String deleteCartItem(@PathVariable(name= "id") Long id,
                                 RedirectAttributes redirectAttributes,
                                 Principal principal) {

        String username = principal.getName();

        CartItem cartItem = cartItemRepository.findById(id).orElse(null);

        if(cartItem != null){

            String productName = cartItem.getProduct().getName();

            cartItemRepository.deleteById(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Đã xóa sản phẩm " + productName + " khỏi giỏ hàng."
            );

            log.info("[User [{}] đang xóa sản phẩm có cartItemId={}]", username, id);
            log.info("[Đã xóa sản phẩm [{}] khỏi giỏ hàng của user [{}]]", productName, username);

        }
        else{
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Không tìm thấy sản phẩm trong giỏ hàng."
            );
        }

        return "redirect:/user/cart";
    }
}
