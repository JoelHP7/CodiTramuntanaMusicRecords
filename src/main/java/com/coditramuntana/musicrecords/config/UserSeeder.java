package com.coditramuntana.musicrecords.config;

import com.coditramuntana.musicrecords.model.data.Role;
import com.coditramuntana.musicrecords.model.data.UserAccount;
import com.coditramuntana.musicrecords.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the example users the first time the application starts.
 *
 * <p>Deliberately separate from {@code DataSeeder} rather than being another method there:
 * {@code DataSeederTest} wipes artists, LPs and authors and then invokes the seeder by hand,
 * but it does not wipe users, so seeding users from the same class would break on the unique
 * constraint of the user name.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seeding.enabled", havingValue = "true", matchIfMissing = true)
public class UserSeeder implements ApplicationRunner {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userAccountRepository.count() > 0) {
            log.info("Users already present, skipping user seeding");
            return;
        }

        log.info("Seeding example users");

        save("admin", "admin123", Role.ADMIN);
        save("user", "user123", Role.USER);

        log.info("User seeding completed: {} users created", userAccountRepository.count());
    }

    private void save(String username, String rawPassword, Role role) {
        userAccountRepository.save(UserAccount.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .enabled(true)
                .build());
    }

}
