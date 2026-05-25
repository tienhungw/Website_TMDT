package com.TH.demo.Controller;

import com.TH.demo.Model.Users;
import com.TH.demo.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class UserController {
    @Autowired
    UserRepository userRepository;

    @GetMapping("/admin/customer")
    public String trangKhachHang(Model model){
        List<Users> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin-khachhang";
    }
}