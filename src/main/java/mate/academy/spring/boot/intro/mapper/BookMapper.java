package mate.academy.spring.boot.intro.mapper;

import mate.academy.spring.boot.intro.config.MapperConfig;
import mate.academy.spring.boot.intro.dto.BookDto;
import mate.academy.spring.boot.intro.dto.CreateBookRequestDto;
import mate.academy.spring.boot.intro.model.Book;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface BookMapper {
    BookDto toDto(Book book);

    Book toModel(CreateBookRequestDto bookDto);
}
