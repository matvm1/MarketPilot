package com.marketpilot.domain.repo;

import java.util.Optional;

public interface BaseRepository<ID, T> {
    Optional<T> findById(ID id);
    boolean save(T entity);
    int count();
}