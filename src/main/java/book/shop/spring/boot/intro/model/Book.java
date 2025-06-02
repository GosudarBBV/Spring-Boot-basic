package book.shop.spring.boot.intro.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Setter
@Getter
@Entity
@SQLDelete(sql = "UPDATE books SET deleted = true WHERE id=?")
@Where(clause = "deleted = false")
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String author;

    @Column(nullable = true, unique = true)
    private String isbn;

    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 1000)
    private String description;

    @Column(length = 255)
    private String coverImage;

    @Column(nullable = false)
    private Boolean deleted;
}
