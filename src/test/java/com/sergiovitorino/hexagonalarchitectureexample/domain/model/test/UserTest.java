package com.sergiovitorino.hexagonalarchitectureexample.domain.model.test;

import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    public void testEqualsWithSameIdAndDifferentNameAreEqual() {
        var id = UUID.randomUUID();
        var user1 = new User(id, "Alice");
        var user2 = new User(id, "Bob");

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    public void testEqualsWithDifferentIdAndSameNameAreNotEqual() {
        var user1 = new User(UUID.randomUUID(), "Alice");
        var user2 = new User(UUID.randomUUID(), "Alice");

        assertNotEquals(user1, user2);
    }

    @Test
    public void testEqualsWithNullIdBothUsersAreEqual() {
        var user1 = new User("Alice");
        var user2 = new User("Bob");

        // Both have null id, so equals(of = "id") treats them as equal
        assertEquals(user1, user2);
    }

    @Test
    public void testEqualsWithSameInstanceIsEqual() {
        var user = new User(UUID.randomUUID(), "Alice");

        assertEquals(user, user);
    }

    @Test
    public void testEqualsWithNullIsNotEqual() {
        var user = new User(UUID.randomUUID(), "Alice");

        assertNotEquals(null, user);
    }

    @Test
    public void testToStringContainsNameAndId() {
        var id = UUID.randomUUID();
        var user = new User(id, "Alice");

        var toString = user.toString();
        assertTrue(toString.contains("Alice"));
        assertTrue(toString.contains(id.toString()));
    }

    @Test
    public void testConstructorWithNameOnly() {
        var user = new User("TestUser");

        assertEquals("TestUser", user.getName());
        assertNull(user.getId());
    }

    @Test
    public void testNoArgsConstructor() {
        var user = new User();

        assertNull(user.getId());
        assertNull(user.getName());
    }
}
