package com.marketpilot.application.ports.persistence;

import java.util.Optional;

public interface BaseRepository<T, ID> {
    Optional<T> findById(ID id);
    void save(T entity);
    int count();
}