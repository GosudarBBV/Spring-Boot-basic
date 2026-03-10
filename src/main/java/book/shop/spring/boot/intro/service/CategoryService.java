package book.shop.spring.boot.intro.service;

import book.shop.spring.boot.intro.dto.CategoryResponseDto;
import book.shop.spring.boot.intro.dto.CreateCategoryRequestDto;
import java.util.List;

public interface CategoryService {
    List<CategoryResponseDto> findAll();

    CategoryResponseDto getById(Long id);

    CategoryResponseDto save(CreateCategoryRequestDto dto);

    CategoryResponseDto update(Long id, CreateCategoryRequestDto dto);

    void deleteById(Long id);
}
