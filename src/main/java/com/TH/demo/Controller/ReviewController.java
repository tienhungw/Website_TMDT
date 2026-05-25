package com.TH.demo.Controller;

import com.TH.demo.Model.OrderDetail;
import com.TH.demo.Model.Product;
import com.TH.demo.Model.Review;
import com.TH.demo.Model.Users;
import com.TH.demo.Repositories.OrderdetailRepository;
import com.TH.demo.Repositories.ProductRepository;
import com.TH.demo.Repositories.ReviewRepository;
import com.TH.demo.Repositories.UserRepository;
import com.TH.demo.Services.OrderDetailServices;
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

import java.security.Principal;
import java.time.LocalDate;

@Controller
public class ReviewController {
    @Autowired
    OrderdetailRepository orderdetailRepository;
    @Autowired
    ProductServices productServices;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ReviewRepository reviewRepository;
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    @GetMapping("/user/review/{orderDetailId}")
    public String showReviewForm(@PathVariable Long orderDetailId, Model model, Principal principal) {
        String username = principal.getName();
        System.out.println(orderDetailId);
        OrderDetail orderDetail = orderdetailRepository.getReferenceById(orderDetailId);
        Product product = productServices.getProductByOrderDetailId(orderDetailId);
        System.out.println(product.getName());
        System.out.println(orderDetail.getProduct().getName());
        model.addAttribute("product", product);
        model.addAttribute("review", new Review());
        model.addAttribute("orderDetail", orderDetail);
        log.info("[USER [{}] truy cập form review cho sản phẩm: {}]", username, product.getName());
        return "trang-review";
    }
    @PostMapping("/user/review/save")
    public String saveReview(@RequestParam Long productId,
                             @RequestParam Long orderDetailId,
                             @RequestParam String content,
                             @RequestParam int rating,
                             Principal principal) {
        Users user = userRepository.findByUsername(principal.getName()).orElseThrow();
        Product product = productRepository.getReferenceById(productId);

        OrderDetail orderDetail = orderdetailRepository.findById(orderDetailId).orElseThrow(()-> new RuntimeException("khong tim thay orderdetail!"));

        Review review = new Review();
        review.setReviewDate(LocalDate.now());
        review.setUser(user);
        review.setProduct(product);
        review.setComment(content);
        review.setRating(rating);

        reviewRepository.save(review);
        orderDetail.setHasReviewed(true);
        orderdetailRepository.save(orderDetail);

        log.info("[USER [{}] đã thêm đánh giá cho sản phẩm: {}]", user.getUsername(), product.getName());

        return "redirect:/user/history";
    }

}
