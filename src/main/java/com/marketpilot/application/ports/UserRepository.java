package com.marketpilot.application.ports;

import com.marketpilot.domain.entities.auth.User;

import java.util.Optional;

public interface UserRepository extends BaseRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
