package book.shop.spring.boot.intro.dto;

public record OrderItemDto(
        Long id,
        Long bookId,
        int quantity
) {
}
