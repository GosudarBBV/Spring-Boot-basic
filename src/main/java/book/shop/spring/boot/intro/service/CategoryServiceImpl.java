package book.shop.spring.boot.intro.service;

import book.shop.spring.boot.intro.dto.CategoryDto;
import book.shop.spring.boot.intro.exception.EntityNotFoundException;
import book.shop.spring.boot.intro.mapper.CategoryMapper;
import book.shop.spring.boot.intro.model.Category;
import book.shop.spring.boot.intro.repository.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    public List<CategoryDto> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public CategoryDto getById(Long id) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        return mapper.toDto(category);
    }

    public CategoryDto save(CategoryDto dto) {
        return mapper.toDto(repository.save(mapper.toEntity(dto)));
    }

    public CategoryDto update(Long id, CategoryDto dto) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        category.setName(dto.name());
        category.setDescription(dto.description());
        return mapper.toDto(repository.save(category));
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
