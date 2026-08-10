package com.nikunj.library.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com.nikunj.library.model.RefreshToken;
import com.nikunj.library.model.User;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user = :user")
    int revokeByUser(@org.springframework.data.repository.query.Param("user") User user);

    @Modifying
    int deleteByUser(User user);
}
