package book.shop.spring.boot.intro.mapper;

import book.shop.spring.boot.intro.config.MapperConfig;
import book.shop.spring.boot.intro.dto.CategoryDto;
import book.shop.spring.boot.intro.model.Category;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface CategoryMapper {
    CategoryDto toDto(Category category);

    Category toEntity(CategoryDto dto);
}
