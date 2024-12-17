package ca.jrvs.apps.stockquote.dao;

import java.util.List;
import java.util.Optional;

public interface CrudeRepository<T, ID> {
    T save(T entity); // Create or update
    Optional<T> findById(ID id); // Read
    List<T> findAll(); // Read All
    void deleteById(ID id); // Delete
}
