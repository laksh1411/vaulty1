package com.example.passwordmanager.service;

import com.example.passwordmanager.model.PasswordEntry;
import com.example.passwordmanager.model.User;
import com.example.passwordmanager.repository.PasswordRepository;
import com.example.passwordmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PasswordService {

    @Autowired
    private PasswordRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EncryptionService encryptionService;

    // get all entries (for backward compatibility)
    public List<PasswordEntry> getAll() {
        return repository.findAll();
    }

    // get all entries for a specific user
    public List<PasswordEntry> getAllForUser(String username) {
        return repository.findAllByOwnerUsernameOrderByCreatedAtDesc(username);
    }

    // save entry without owner (legacy)
    public void save(PasswordEntry entry) {
        String plainPassword = entry.getPassword();
        entry.setPassword(encryptionService.encrypt(plainPassword));
        repository.save(entry);
    }

    // save entry for a specific user (with encryption)
    public void saveForUser(PasswordEntry entry, String username) {
        Optional<User> maybe = userRepository.findByUsername(username);
        if (maybe.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        User owner = maybe.get();
        entry.setOwner(owner);
        String plainPassword = entry.getPassword();
        entry.setPassword(encryptionService.encrypt(plainPassword));
        repository.save(entry);
    }

    // save an already-fetched entity (optionally re-encrypting password)
    public void saveEntity(PasswordEntry entry, boolean reencrypt) {
        if (reencrypt && entry.getPassword() != null) {
            entry.setPassword(encryptionService.encrypt(entry.getPassword()));
        }
        repository.save(entry);
    }

    // delete by id (legacy)
    public void delete(Long id) {
        repository.deleteById(id);
    }

    // delete by id for a specific user
    public void deleteForUser(Long id, String username) {
        Optional<PasswordEntry> maybe = repository.findByIdAndOwnerUsername(id, username);
        if (maybe.isPresent()) {
            repository.deleteById(id);
        }
    }

    // find entry by id and owner username
    public Optional<PasswordEntry> findByIdAndOwner(Long id, String username) {
        return repository.findByIdAndOwnerUsername(id, username);
    }

    // get decrypted password for a specific entry
    public String getDecryptedPassword(Long id) {
        Optional<PasswordEntry> maybe = repository.findById(id);
        if (maybe.isEmpty()) {
            return "";
        }
        return encryptionService.decrypt(maybe.get().getPassword());
    }
}
