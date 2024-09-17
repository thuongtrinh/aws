package com.txt.aws.s3.service.impl;

import com.txt.aws.s3.config.Constant;
import com.txt.aws.s3.dto.ObjectRequest;
import com.txt.aws.s3.repository.TodoRepository;
import com.txt.aws.s3.config.BucketName;
import com.txt.aws.s3.entity.Todo;
import com.txt.aws.s3.service.FileStore;
import com.txt.aws.s3.service.S3SelectService;
import com.txt.aws.s3.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.apache.http.entity.ContentType.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TodoServiceImpl implements TodoService {
    private final FileStore fileStore;
    private final TodoRepository todoRepository;
    private final S3SelectService s3SelectService;


    @Override
    public Todo saveTodo(String title, String description, MultipartFile file) {
        //check if the file is empty
        if (file.isEmpty()) {
            throw new IllegalStateException("Cannot upload empty file");
        }
        //Check if the file is an image
        if (!Arrays.asList(IMAGE_PNG.getMimeType(),
                IMAGE_BMP.getMimeType(),
                IMAGE_GIF.getMimeType(),
                IMAGE_JPEG.getMimeType()).contains(file.getContentType())) {
            throw new IllegalStateException("FIle uploaded is not an image");
        }
        //get file metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", file.getContentType());
        metadata.put("Content-Length", String.valueOf(file.getSize()));
        //Save Image in S3 and then save Todo in the database
//        String path = String.format("%s/%s", BucketName.TODO_IMAGE.getBucketName(), UUID.randomUUID());
        String path = String.format("%s/%s", BucketName.TODO_IMAGE.getBucketName(), Constant.AWS_S3_PATH.TODO);
        String fileName = String.format("%s", file.getOriginalFilename());
        try {
            fileStore.upload(path, fileName, Optional.of(metadata), file.getInputStream());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to upload file", e);
        }

        Todo todo = todoRepository.findFirstByImagePathAndImageFileName(path, fileName);
        if (Objects.nonNull(todo)) {
            todo.setDescription(description);
            todo.setImageFileName(fileName);
            todo.setTitle(title);
        } else {
            todo = Todo.builder()
                    .description(description)
                    .title(title)
                    .imagePath(path)
                    .imageFileName(fileName)
                    .build();
        }

        Todo todoResult = todoRepository.save(todo);
        return todoResult;
    }

    @Override
    public byte[] downloadTodoImage(Long id) {
        Todo todo = todoRepository.findById(id).get();
        return fileStore.download(todo.getImagePath(), todo.getImageFileName());
    }

    @Override
    public List<Todo> getAllTodos() {
        List<Todo> todos = new ArrayList<>();
        todoRepository.findAll().forEach(todos::add);
        return todos;
    }

    @Override
    public Boolean deleteObject(ObjectRequest objectRequest) {
        return fileStore.deleteObject(objectRequest);
    }

    @Override
    public Boolean uploadCommon(String bucketName, String pathRequest, MultipartFile file) {
        try { //check if the file is empty
            if (Objects.isNull(file) || file.isEmpty()) {
                throw new IllegalStateException("Cannot upload empty file");
            } else if (StringUtils.isEmpty(bucketName)) {
                throw new IllegalStateException("Error empty of bucketName");
            }

            //get file metadata
            Map<String, String> metadata = new HashMap<>();
            metadata.put("Content-Type", file.getContentType());
            metadata.put("Content-Length", String.valueOf(file.getSize()));
            String path = bucketName;
            if(StringUtils.isNotBlank(pathRequest)) {
                path = String.format("%s/%s", bucketName, pathRequest);
            }
            String fileName = String.format("%s", file.getOriginalFilename());

            fileStore.upload(path, fileName, Optional.of(metadata), file.getInputStream());
        } catch (IOException e) {
            throw new IllegalStateException("UploadCommon Failed to upload file", e);
        }
        return true;
    }

    @Override
    public Object s3SelectObject() throws Exception {
        s3SelectService.amazonS3SelectObject("");
        return null;
    }
}
