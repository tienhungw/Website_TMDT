package com.TH.demo.Controller;

import com.TH.demo.Cloud.CloudinaryUploadResult;
import com.TH.demo.Model.*;
//import com.TH.demo.Services.ICategory;
//import com.TH.demo.Services.IProduct;
import com.TH.demo.Repositories.CartItemRepository;
import com.TH.demo.Repositories.ReviewRepository;
import com.TH.demo.Repositories.UserRepository;
import com.TH.demo.Services.CartServices;
import com.TH.demo.Services.CategoryServices;
import com.TH.demo.Services.CloudinaryServices;
import com.TH.demo.Services.ProductServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.sound.sampled.ReverbType;
import javax.sql.rowset.CachedRowSet;
import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Controller
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    @Autowired
    ProductServices productServices;

    @Autowired
    CategoryServices categoryServices;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CartServices cartServices;
    @Autowired
    CartItemRepository cartItemRepository;
    @Autowired
    CloudinaryServices cloudinaryServices;
    @Autowired
    ReviewRepository reviewRepository;

    @GetMapping("/admin/products")
    public String adminProducts(Model m,
                                @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "8") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productServices.getAllProduct(pageable);
        m.addAttribute("Products", productPage.getContent());
        m.addAttribute("currentPage", page);
        m.addAttribute("totalPages", productPage.getTotalPages());
        m.addAttribute("size", size);

        logger.info("[Found {} products]", productPage.getTotalElements());
        logger.info("[Found {} products in this page]", productPage.getNumberOfElements());
        productPage.getContent().forEach(product -> logger.info("[Product name: {}]", product.getName()));
        return "admin";
    }

//    GetMapping("/user/home")
//    public String listProductUser(Model m,
//                                  @RequestParam(defaultValue = "0") int page,
//                                  @RequestParam(defaultValue = "8") int size) {
//        List<Category> categories = categoryServices.getAllCategory();
//        Pageable pageable = PageRequest.of(page, size);
//        Page<Product> productPage = productServices.getAllProduct(pageable);
//
//        m.addAttribute("categories", categories);
//        m.addAttribute("listProduct", productPage.getContent());
//        m.addAttribute("currentPage", page);
//        m.addAttribute("totalPages", productPage.getTotalPages());
//        m.addAttribute("size", size);
//
//        logger.info("[Found {} products]", productPage.getTotalElements());
//        logger.info("[Found {} products in this page]", productPage.getNumberOfElements());
//        productPage.getContent().forEach(product -> logger.info("[Product name: {}]", product.getName()));
//
//        return "trangchu";
//    }



    // Them san pham
    @GetMapping("/admin/new-products")
    public String trangThemSanPham(Model model) {
        List<Category> categories = categoryServices.getAllCategory();
        model.addAttribute("categories", categories);
        return "admin-themsanpham";
    }
    @PostMapping("/admin/new-products")
    public String themSanPham(
            @RequestParam("ten") String ten,
            @RequestParam("chonLoai") Long chonLoai,
            @RequestParam("giaban") double giaBan,
            @RequestParam("tonkho") int tonKho,
            @RequestParam("fileInput") MultipartFile fileInput,
            @RequestParam("mota") String moTa,
            Model m) throws IOException {

        Category category = categoryServices.findById(chonLoai)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        CloudinaryUploadResult uploadResult = cloudinaryServices.uploadFileWithPublicId(fileInput);

        Product product = new Product();
        product.setName(ten);
        product.setCategory(category);
        product.setPrice(giaBan);
        product.setQuantity(tonKho);
        product.setImageUrl(uploadResult.getUrl());
        product.setImagePublicId(uploadResult.getPublicId());
        product.setDescription(moTa);
        productServices.saveProduct(product);

        logger.info("[ADMIN Đã thêm product:{}]", product.getName());

        return "redirect:/admin/products";
    }

    // Sua
    @GetMapping("/admin/edit-products/{id}")
    public String editProduct(@PathVariable(name = "id") Long id, Model m){
        List<Category> listCategory = categoryServices.getAllCategory();
        m.addAttribute("categories", listCategory);

        Product p = productServices.getProductById(id);
        if (p == null) {return "redirect:/products";}
        m.addAttribute("product", p);
        logger.info("[ADMIN dang sua san pham {}]", p.getName());
        return "admin-chinhsua";
    }
    @PostMapping("/admin/save")
    public String saveProduct(@ModelAttribute("product") Product product,
                              @RequestParam("category.id") Long categoryId,
                              @RequestParam("fileInput") MultipartFile fileInput,
                              RedirectAttributes redirectAttributes) throws IOException {
        Product existingProduct = productServices.getProductById(product.getId());

        existingProduct.setName(product.getName());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setQuantity(product.getQuantity());
        existingProduct.setDescription(product.getDescription());

        Category category = categoryServices.findById(categoryId).orElseThrow();
        existingProduct.setCategory(category);

        //xoa anh cu, luu anh moi
        if (fileInput != null && !fileInput.isEmpty()) {
            //xoa
            String oldPublicId = existingProduct.getImagePublicId();
            if (oldPublicId != null && !oldPublicId.isEmpty()) {
//                cloudinaryServices.deleteFile(oldPublicId);
                cloudinaryServices.deleteFile(oldPublicId);
            }

            //luu
            CloudinaryUploadResult uploadResult = cloudinaryServices.uploadFileWithPublicId(fileInput);
            existingProduct.setImageUrl(uploadResult.getUrl());
            existingProduct.setImagePublicId(uploadResult.getPublicId());
        }
        productServices.saveProduct(existingProduct);
        logger.info("[ADMIN da sua thanh cong san pham {}]", product.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Sửa sản phẩm thành công!");
        return "redirect:/admin/products";
    }

    //Xoa
    @GetMapping("/admin/delete-products/{id}")
    public String deleteProduct(@PathVariable(name = "id") Long id) {
        Product product = productServices.getProductById(id);
        if (product != null) {
            String publicId = product.getImagePublicId();
            if (publicId != null && !publicId.isEmpty()) {
                try {
                    cloudinaryServices.deleteFile(publicId);
                } catch (Exception e) {}
            } else{}
            productServices.deleteProductById(id);

        } else {
            logger.info("[khong tim thay san pham de xoa!]");
        }
        logger.info("[ADMIN Đã xóa thành công sản phẩm {}]", product.getName());
        return "redirect:/admin/products";
    }

//Phan trang
    @GetMapping("/user/home")
    public String listProductUser(Model m,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "8") int size) {
        List<Category> categories = categoryServices.getAllCategory();
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productServices.getAllProduct(pageable);

        m.addAttribute("categories", categories);
        m.addAttribute("listProduct", productPage.getContent());
        m.addAttribute("currentPage", page);
        m.addAttribute("totalPages", productPage.getTotalPages());
        m.addAttribute("size", size);

        logger.info("[Found {} products]", productPage.getTotalElements());
        logger.info("[Found {} products in this page]", productPage.getNumberOfElements());
        productPage.getContent().forEach(product -> logger.info("[Product name: {}]", product.getName()));

        return "trangchu";
    }

    @GetMapping("user/about")
    public String trangThongTin(){
        return "thongtin";
    }

    //Tim kiem
    @GetMapping("user/home/search")
    public String timKiem(@RequestParam("keyword") String keyword, Model model,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "16") int size){
        List<Category> categoryList = categoryServices.getAllCategory();

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productServices.searchProducts(keyword, pageable);
        model.addAttribute("listProduct", products.getContent());
        model.addAttribute("categories", categoryList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("size", size);
        return "trangchu";
    }
//    @GetMapping("user/home/search")
//    public String timKiem(@RequestParam("keyword") String keyword, Model model, Pageable pageable){
//        Page<Product> products = productServices.searchProducts(keyword, pageable);
//        model.addAttribute("listProduct", products);
//        logger.info("[Found {} products]", products.getTotalElements());
//        products.getContent().forEach(product -> logger.info("[Product name: {}]", product.getName()));
//        return "trangchu";
//    }
// Loc --> loi
//    @GetMapping("user/home/category")
//    public String locTheoCategory(@RequestParam(name = "categoryId") Long categoryID, Model m,
//                                  @RequestParam(defaultValue = "0") int page,
//                                  @RequestParam(defaultValue = "16") int size){
//        List<Category> categoryList = categoryServices.getAllCategory();
//        Category category = categoryServices.getCategoryById(categoryID);
//        Pageable pageable = PageRequest.of(page,size);
//        Page<Product> productList = productServices.getProductsByCategory(category, pageable);
//        m.addAttribute("categories", categoryList);
//        m.addAttribute("listProduct", productList);
//        logger.info("[Found {} products]", productList.getTotalElements());
//        productList.getContent().forEach(product -> logger.info("[Product name: {}]", product.getName()));
//        return "trangchu";
//    }
    @GetMapping("/user/home/category")
    public String locTheoCategory(
            @RequestParam(name = "categoryId") Long categoryID,
            Model m,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "16") int size
    ) {
        List<Category> categoryList = categoryServices.getAllCategory();
        Category category = categoryServices.getCategoryById(categoryID);
        if (category == null) {
            m.addAttribute("categories", categoryList);
            m.addAttribute("listProduct", List.of());
            m.addAttribute("error", "Category not found");
            return "trangchu";
        }
        System.out.println(category.getName());
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productList = productServices.getProductsByCategory(category, pageable);
        m.addAttribute("categories", categoryList);
        m.addAttribute("listProduct", productList.getContent());
        m.addAttribute("currentPage", page);
        m.addAttribute("totalPages", productList.getTotalPages());
        m.addAttribute("size", size);
        for(Product p : productList.getContent()){
            System.out.println(p.getName());
        }
        return "trangchu";
    }

    //Chi tiet sp
    @GetMapping("/user/home/{id}")
    public String getProductDetail(@PathVariable Long id, Model model) {
        Product product = productServices.getProductById(id);
        List<Review> reviews = reviewRepository.findByProduct(product);
        model.addAttribute("reviews", reviews);
        model.addAttribute("product", product);
        logger.info("[USER đang xem sản phẩm: {}]", product.getName());
        return "chitietsanpham";
    }
}



