package com.TH.demo.Services;

import com.TH.demo.Cloud.CloudinaryUploadResult;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryServices {
    @Autowired
    private Cloudinary cloudinary;

    public CloudinaryUploadResult uploadFileWithPublicId(MultipartFile file) throws IOException {
        Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        CloudinaryUploadResult uploadResult = new CloudinaryUploadResult();
        uploadResult.setUrl(result.get("secure_url").toString());
        uploadResult.setPublicId(result.get("public_id").toString());
        return uploadResult;
    }

    public void deleteFile(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}
