package com.marketpilot.domain.repo;

import java.util.Optional;

public interface BaseRepository<T, ID> {
    Optional<T> findById(ID id);
    boolean save(T entity);
    int count();
}