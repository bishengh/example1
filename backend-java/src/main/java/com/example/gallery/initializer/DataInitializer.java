package com.example.gallery.initializer;

import com.example.gallery.model.User;
import com.example.gallery.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        userRepository.findByUsername("admin")
                .orElseGet(() -> userRepository.save(new User("admin", "password")));
    }
}
