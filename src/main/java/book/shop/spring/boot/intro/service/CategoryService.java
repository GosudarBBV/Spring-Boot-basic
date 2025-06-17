package book.shop.spring.boot.intro.service;

import book.shop.spring.boot.intro.dto.CategoryDto;
import java.util.List;

public interface CategoryService {
    List<CategoryDto> findAll();

    CategoryDto getById(Long id);

    CategoryDto save(CategoryDto dto);

    CategoryDto update(Long id, CategoryDto dto);

    void deleteById(Long id);
}
