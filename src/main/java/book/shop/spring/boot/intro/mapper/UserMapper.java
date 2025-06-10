package book.shop.spring.boot.intro.mapper;

import book.shop.spring.boot.intro.dto.UserRegistrationRequestDto;
import book.shop.spring.boot.intro.dto.UserResponseDto;
import book.shop.spring.boot.intro.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toModel(UserRegistrationRequestDto dto);

    UserResponseDto toResponseDto(User user);
}
