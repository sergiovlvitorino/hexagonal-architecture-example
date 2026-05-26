package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.persistence;

import com.sergiovitorino.hexagonalarchitectureexample.application.service.UserService;
import com.sergiovitorino.hexagonalarchitectureexample.domain.exception.UserNotFoundException;
import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@SpringBootTest
@ActiveProfiles({"it", "prod"})
@Transactional
class UserRepositoryPostgresIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Autowired
    private UserService userService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flyway_v1MigrationApplied_usersTableExists() {
        var count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '1' AND success = true",
                Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void save_userIsPersistedInPostgres() {
        User savedUser = userService.save(new User("Test User Postgres"));

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("Test User Postgres");
    }

    @Test
    void findById_existingId_returnsUserFromPostgres() {
        User savedUser = userService.save(new User("FindById Test Postgres"));

        User foundUser = userService.findById(savedUser.getId());

        assertThat(foundUser.getName()).isEqualTo("FindById Test Postgres");
    }

    @Test
    void findById_nonExistingId_throwsUserNotFoundException() {
        UUID randomId = UUID.randomUUID();

        assertThrows(UserNotFoundException.class, () -> userService.findById(randomId));
    }

    @Test
    void findAll_withNameFilter_returnsOnlyMatchingUsers() {
        userService.save(new User("UniqueFilterNameXYZ"));
        userService.save(new User("OtherNameABC"));

        var page = userService.findAll(0, 10, "name", true, "UniqueFilterNameXYZ");

        assertThat(page.getContent())
                .extracting(User::getName)
                .contains("UniqueFilterNameXYZ")
                .doesNotContain("OtherNameABC");
    }

    @Test
    void deleteById_existingUserIsDeleted() {
        User savedUser = userService.save(new User("Delete Test Postgres"));

        userService.delete(savedUser.getId());

        assertThrows(UserNotFoundException.class, () -> userService.findById(savedUser.getId()));
    }

    @Test
    void deleteById_nonExistingUser_throwsUserNotFoundException() {
        UUID nonExistingId = UUID.randomUUID();

        assertThrows(UserNotFoundException.class, () -> userService.delete(nonExistingId));
    }

    @Test
    void findAll_orderByNameDescending_returnsUsersInDescendingOrder() {
        userService.save(new User("Alpha User"));
        userService.save(new User("Zeta User"));
        userService.save(new User("Mu User"));

        var page = userService.findAll(0, 10, "name", false, null);

        List<String> names = page.getContent().stream().map(User::getName).toList();
        assertThat(names).isSortedAccordingTo((a, b) -> b.compareToIgnoreCase(a));
    }

    @Test
    void schema_idxUserNameIndex_exists() {
        var indexName = jdbcTemplate.queryForObject(
                "SELECT indexname FROM pg_indexes WHERE tablename = 'users' AND indexname = 'idx_user_name'",
                String.class);

        assertThat(indexName).isEqualTo("idx_user_name");
    }
}
