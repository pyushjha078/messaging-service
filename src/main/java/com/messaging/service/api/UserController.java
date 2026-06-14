package com.messaging.service.api;

import com.messaging.service.api.dto.UserSummary;
import com.messaging.service.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/{username}")
    ResponseEntity<UserSummary> findByUsername(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(user -> ResponseEntity.ok(new UserSummary(user.getId(), user.getUsername())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
