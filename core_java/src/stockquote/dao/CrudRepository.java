package stockquote.dao;

import java.util.List;

public interface CrudRepository<T, ID> {
    T save(T entity); //Create or update
    T findById(ID id); //Read
    List<T> findAll(); // Read All
    boolean deteleById(ID id); //Delete
}