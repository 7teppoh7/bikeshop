package ru.labs.offerservice.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.labs.offerservice.entities.Category;

public interface CategoryRepository extends CrudRepository<Category, Integer> {
}
