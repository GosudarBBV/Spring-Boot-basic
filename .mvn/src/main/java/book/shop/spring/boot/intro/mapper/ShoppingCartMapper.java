package book.shop.spring.boot.intro.mapper;

import book.shop.spring.boot.intro.config.MapperConfig;
import book.shop.spring.boot.intro.dto.CartItemResponseDto;
import book.shop.spring.boot.intro.dto.ShoppingCartResponseDto;
import book.shop.spring.boot.intro.model.CartItem;
import book.shop.spring.boot.intro.model.ShoppingCart;
import java.util.Set;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface ShoppingCartMapper {
    ShoppingCartResponseDto toDto(ShoppingCart shoppingCart);

    Set<CartItemResponseDto> toCartItemDtos(Set<CartItem> items);

    CartItemResponseDto toCartItemDto(CartItem cartItem);
}
