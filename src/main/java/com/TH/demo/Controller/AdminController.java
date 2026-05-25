//package com.TH.demo.Controller;
//
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//
//@Controller
//@RequestMapping("/admin")
//public class AdminController {
//
//    @GetMapping("/dashboard")
//    public String adminDashboard() {
//        return "admin/dashboard";
//
package com.TH.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Controller
public class AdminController {

    @GetMapping("/admin/new-ctkm")
    public String newQCPage() {
        return "admin-ctkm";
    }

    @PostMapping("/admin/save-ctkm")
    public String saveQC(
            @RequestParam("qc1") MultipartFile qc1,
            @RequestParam("qc2") MultipartFile qc2,
            @RequestParam("qc3") MultipartFile qc3,
            @RequestParam("qc4") MultipartFile qc4
    ) throws Exception {

        String path = System.getProperty("user.dir")
                + "/target/classes/static/img/";

        File folder = new File(path);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        qc1.transferTo(new File(path + "qc1.jpg"));
        qc2.transferTo(new File(path + "qc2.jpg"));
        qc3.transferTo(new File(path + "qc3.jpg"));
        qc4.transferTo(new File(path + "qc4.jpg"));

        return "redirect:/admin/products";
    }

}