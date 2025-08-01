package book.shop.spring.boot.intro.repository;

import static org.assertj.core.api.Assertions.assertThat;

import book.shop.spring.boot.intro.model.Role;
import book.shop.spring.boot.intro.model.RoleName;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:database/schemas/roles-schema.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/roles/add-roles.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/roles/clear-roles.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("findByName should return role if exists")
    void findByName_ReturnsRole() {
        Optional<Role> found = roleRepository.findByName(RoleName.USER);

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(RoleName.USER);
    }
}
