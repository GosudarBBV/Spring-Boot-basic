package book.shop.spring.boot.intro.controller;

import book.shop.spring.boot.intro.dto.BookDtoWithoutCategoryIds;
import book.shop.spring.boot.intro.dto.CategoryDto;
import book.shop.spring.boot.intro.service.BookService;
import book.shop.spring.boot.intro.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Operations related to book categories")
public class CategoryController {
    private final CategoryService service;
    private final BookService bookService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Create a new category", description = "Available only for ADMIN role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    public CategoryDto create(@RequestBody @Valid CategoryDto dto) {
        return service.save(dto);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping
    @Operation(summary = "Get all categories", description = "Available for ADMIN and USER roles")
    public List<CategoryDto> getAll() {
        return service.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Returns category details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public CategoryDto getById(
            @Parameter(description = "Category ID to retrieve") @PathVariable Long id) {
        return service.getById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update a category", description = "Available only for ADMIN role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public CategoryDto update(
            @Parameter(description = "ID of the category to update") @PathVariable Long id,
            @RequestBody @Valid CategoryDto dto) {
        return service.update(id, dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category", description = "Available only for ADMIN role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public void delete(
            @Parameter(description = "ID of the category to delete")
            @PathVariable Long id) {
        service.deleteById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{id}/books")
    @Operation(summary = "Get books by category",
            description = "Returns all books belonging to a specific category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Books returned successfully"),
            @ApiResponse(responseCode = "404",
                    description = "Category not found")
    })
    public List<BookDtoWithoutCategoryIds> getBooksByCategoryId(
            @Parameter(description = "ID of the category") @PathVariable Long id) {
        return bookService.findAllByCategoryId(id);
    }
}
