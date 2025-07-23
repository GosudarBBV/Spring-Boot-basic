package book.shop.spring.boot.intro.repository;

import book.shop.spring.boot.intro.model.Role;
import book.shop.spring.boot.intro.model.RoleName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class RoleRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("findByName should return role if exists")
    void findByName_ReturnsRole() {
        if (roleRepository.findByName(RoleName.USER).isEmpty()) {
            Role role = new Role();
            role.setName(RoleName.USER);
            roleRepository.save(role);
        }

        Optional<Role> found = roleRepository.findByName(RoleName.USER);

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(RoleName.USER);
    }
}
