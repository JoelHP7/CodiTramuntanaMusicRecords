package com.coditramuntana.musicrecords.repository;

import com.coditramuntana.musicrecords.model.data.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Query("select rt from RefreshToken rt join fetch rt.user where rt.token = :token")
    Optional<RefreshToken> findByTokenWithUser(@Param("token") String token);

    /**
     * Revokes every token of a user in a single statement, used when logging out everywhere
     * and to keep the table from growing without bound.
     */
    @Modifying
    @Query("update RefreshToken rt set rt.revoked = true where rt.user.id = :userId and rt.revoked = false")
    int revokeAllByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("delete from RefreshToken rt where rt.expiresAt < :nowEpochMs")
    int deleteExpired(@Param("nowEpochMs") long nowEpochMs);

}
