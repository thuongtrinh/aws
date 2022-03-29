package com.txt.springamazon.repositories;

import com.txt.springamazon.domain.Todo;
import org.springframework.data.repository.CrudRepository;

public interface TodoRepository extends CrudRepository<Todo, Long> {
    Todo findByTitle(String title);
}
