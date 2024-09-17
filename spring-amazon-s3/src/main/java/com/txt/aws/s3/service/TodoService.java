package com.txt.aws.s3.service;

import com.txt.aws.s3.dto.ObjectRequest;
import com.txt.aws.s3.entity.Todo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TodoService {
    Todo saveTodo(String title, String description, MultipartFile file);

    byte[] downloadTodoImage(Long id);

    List<Todo> getAllTodos();

    Boolean deleteObject(ObjectRequest objectRequest);

    Boolean uploadCommon(String bucketName, String path, MultipartFile file);

    Object s3SelectObject() throws Exception;
}