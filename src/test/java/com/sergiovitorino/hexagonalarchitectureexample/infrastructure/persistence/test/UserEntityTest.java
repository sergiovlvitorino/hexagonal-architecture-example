package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.persistence.test;

import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import com.sergiovitorino.hexagonalarchitectureexample.infrastructure.persistence.UserEntity;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UserEntityTest {

    @Test
    public void testToDomainMapsAllFields() {
        var id = UUID.randomUUID();
        var entity = new UserEntity(id, "Alice");
        var user = entity.toDomain();
        assertEquals(id, user.getId());
        assertEquals("Alice", user.getName());
    }

    @Test
    public void testToDomainWithNullIdReturnsUserWithNullId() {
        var entity = new UserEntity("Alice");
        var user = entity.toDomain();
        assertNull(user.getId());
        assertEquals("Alice", user.getName());
    }

    @Test
    public void testFromDomainMapsAllFields() {
        var id = UUID.randomUUID();
        var user = new User(id, "Bob");
        var entity = UserEntity.fromDomain(user);
        assertEquals(id, entity.getId());
        assertEquals("Bob", entity.getName());
    }

    @Test
    public void testFromDomainWithNullIdReturnsEntityWithNullId() {
        var user = new User("Charlie");
        var entity = UserEntity.fromDomain(user);
        assertNull(entity.getId());
        assertEquals("Charlie", entity.getName());
    }

    @Test
    public void testRoundtripPreservesData() {
        var id = UUID.randomUUID();
        var original = new UserEntity(id, "Diana");
        var roundtripped = UserEntity.fromDomain(original.toDomain());
        assertEquals(original.getId(), roundtripped.getId());
        assertEquals(original.getName(), roundtripped.getName());
    }

    @Test
    public void testSingleArgConstructorSetsNameOnly() {
        var entity = new UserEntity("Eve");
        assertNull(entity.getId());
        assertEquals("Eve", entity.getName());
    }

    @Test
    public void testNoArgsConstructorCreatesEmptyEntity() {
        var entity = new UserEntity();
        assertNull(entity.getId());
        assertNull(entity.getName());
    }

    @Test
    public void testEqualsById() {
        var id = UUID.randomUUID();
        var entity1 = new UserEntity(id, "Alice");
        var entity2 = new UserEntity(id, "Bob");
        assertEquals(entity1, entity2);
    }
}
