package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.persistence;

import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = @Index(name = "idx_user_name", columnList = "name"))
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    public UserEntity(String name) {
        this.name = name;
    }

    public User toDomain() {
        return new User(id, name);
    }

    public static UserEntity fromDomain(User user) {
        return new UserEntity(user.getId(), user.getName());
    }
}
