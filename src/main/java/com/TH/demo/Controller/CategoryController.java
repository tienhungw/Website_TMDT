package com.TH.demo.Controller;
import com.TH.demo.Model.Category;
import com.TH.demo.Services.CategoryServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class CategoryController {
    @Autowired
    CategoryServices categoryServices;

    @GetMapping("admin/category/new")
    public String themLoai(){
        return "admin-themsanpham";
    }
    @PostMapping("admin/category/new")
    public String themLoai1(
            @RequestParam("name") String name
    ){
        Category category = new Category();
        category.setName(name);
        categoryServices.saveCategory(category);
        return "redirect:/admin/new-products";
    }
}
