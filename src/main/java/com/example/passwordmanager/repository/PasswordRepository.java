package com.example.passwordmanager.repository;

import com.example.passwordmanager.model.PasswordEntry;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordRepository extends JpaRepository<PasswordEntry, Long> {
    List<PasswordEntry> findAllByOwnerUsernameOrderByCreatedAtDesc(String username);

    Optional<PasswordEntry> findByIdAndOwnerUsername(Long id, String username);
}