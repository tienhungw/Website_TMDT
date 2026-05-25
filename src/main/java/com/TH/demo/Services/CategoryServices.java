package com.TH.demo.Services;

import com.TH.demo.Model.Category;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface CategoryServices {
    public List<Category> getAllCategory();
    public Category getCategoryById(Long id);
    public void saveCategory(Category category);
    public void deleteCategoryById(Long id);

    Optional<Category> findById(Long id);

}
