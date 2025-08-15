package net.javaguides.todo.repository;

import net.javaguides.todo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByUsername(String username);//standard naming convention use to create Spring data JPA custom

    Boolean existsByEmail(String email);//check whether a User object

    Optional<User> findByUsernameOrEmail(String username,String email);

    Boolean existsByUsername(String username);

}
