package com.txt.aws.s3.repository;

import com.txt.aws.s3.entity.Todo;
import org.springframework.data.repository.CrudRepository;

public interface TodoRepository extends CrudRepository<Todo, Long> {
    Todo findByTitle(String title);
    Todo findFirstByImagePathAndImageFileName(String imagePath, String imageFileName);
}
