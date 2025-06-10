package book.shop.spring.boot.intro.repository;

import book.shop.spring.boot.intro.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
}
