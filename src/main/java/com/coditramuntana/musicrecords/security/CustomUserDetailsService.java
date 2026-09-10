package com.coditramuntana.musicrecords.security;

import com.coditramuntana.musicrecords.model.data.UserAccount;
import com.coditramuntana.musicrecords.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Loads users from the database for Spring Security.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        UserAccount account = userAccountRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> {
                    log.warn("Authentication attempt for unknown user '{}'", username);
                    return new UsernameNotFoundException("User '%s' was not found".formatted(username));
                });

        return User.withUsername(account.getUsername())
                .password(account.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority(account.getRole().authority())))
                .disabled(!account.isEnabled())
                .build();
    }

}
