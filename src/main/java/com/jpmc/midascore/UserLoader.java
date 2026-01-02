package com.jpmc.midascore;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class UserLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public UserLoader(UserRepository userRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        ClassPathResource resource = new ClassPathResource("users.json");
        if (!resource.exists()) return;

        try (InputStream is = resource.getInputStream()) {
            List<UserRecord> users = objectMapper.readValue(is, new TypeReference<List<UserRecord>>() {});
            userRepository.saveAll(users);
        }
    }
}
