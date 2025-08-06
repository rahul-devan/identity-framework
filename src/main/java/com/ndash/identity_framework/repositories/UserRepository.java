package com.ndash.identity_framework.repositories;

import com.ndash.identity_framework.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    Optional<User> findByAzureId(String azureId);
    Optional<User> findByEmail(String email);
    List<User> findByActiveTrue();
    Page<User> findByUsernameContainingIgnoreCaseAndActiveTrue(String username, Pageable pageable);


}
