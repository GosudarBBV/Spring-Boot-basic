package book.shop.spring.boot.intro.mapper;

import book.shop.spring.boot.intro.config.MapperConfig;
import book.shop.spring.boot.intro.dto.UserRegistrationRequestDto;
import book.shop.spring.boot.intro.dto.UserResponseDto;
import book.shop.spring.boot.intro.model.User;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    User toModel(UserRegistrationRequestDto user);

    UserResponseDto toResponseDto(User user);
}
