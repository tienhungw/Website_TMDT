package com.TH.demo.Services;

import com.TH.demo.Model.Category;
import com.TH.demo.Repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ImplCategoryServices implements CategoryServices{
    @Autowired
    CategoryRepository categoryRepository;
    @Override
    public List<Category> getAllCategory(){
        return categoryRepository.findAll();
    }
    @Override
    public Category getCategoryById(Long id){
        return categoryRepository.findById(id).orElse(null);
    }
    @Override
    public void saveCategory(Category category){
        categoryRepository.save(category);
    }
    @Override
    public void deleteCategoryById(Long id){
        categoryRepository.deleteById(id);
    }

    @Override
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }
}
