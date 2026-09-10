package com.coditramuntana.musicrecords.repository;

import com.coditramuntana.musicrecords.model.data.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    /**
     * SQLite unique indexes are case sensitive, so lookups are done ignoring case to make
     * usernames behave as users expect.
     */
    Optional<UserAccount> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

}
