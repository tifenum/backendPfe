package com.pfe.users.service;

import com.pfe.users.repository.UserRepository;
import com.pfe.users.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    public Mono<User> registerUser(User user) {
        return userRepository.findByEmail(user.getEmail())
                .flatMap(existingUser ->
                        // Explicitly type the error to match Mono<User>
                        Mono.<User>error(new RuntimeException("Email already in use"))
                )
                .switchIfEmpty(Mono.defer(() -> {
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                    return userRepository.save(user);
                }));
    }

    public Mono<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}