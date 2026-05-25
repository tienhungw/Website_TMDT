package com.TH.demo.Cloud;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {
    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dbkulbopp",
                "api_key", "211237417328866",
                "api_secret", "XQYcZrOgdM9t21poFJe5lgKPY30"));
    }
}