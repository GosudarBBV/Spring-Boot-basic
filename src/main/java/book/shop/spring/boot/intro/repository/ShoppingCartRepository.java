package book.shop.spring.boot.intro.repository;

import book.shop.spring.boot.intro.model.ShoppingCart;
import book.shop.spring.boot.intro.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    Optional<ShoppingCart> findByUser(User user);

    boolean existsByUser(User user);
}
