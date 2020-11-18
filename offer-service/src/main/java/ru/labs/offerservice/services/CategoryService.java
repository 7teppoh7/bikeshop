package ru.labs.offerservice.services;

import org.springframework.stereotype.Service;
import ru.labs.offerservice.entities.Category;
import ru.labs.offerservice.repositories.CategoryRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category findCategoryById(Integer id) {
        return categoryRepository.findById(id).orElse(null);
    }
}
