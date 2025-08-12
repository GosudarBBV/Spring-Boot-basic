package book.shop.spring.boot.intro.repository;

import book.shop.spring.boot.intro.model.Order;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findAllByUserId(Long userId, Pageable pageable);

    Optional<Order> findByIdAndUserId(Long orderId, Long userId);
}
