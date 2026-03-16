package book.shop.spring.boot.intro.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CategoryDto {
    private Long id;
    private String name;
    private String description;
}
