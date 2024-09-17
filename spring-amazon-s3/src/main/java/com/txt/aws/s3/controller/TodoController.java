package com.txt.aws.s3.controller;

import com.txt.aws.s3.dto.ObjectRequest;
import com.txt.aws.s3.entity.Todo;
import com.txt.aws.s3.service.TodoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/v1/todo")
@CrossOrigin("*")
@Tag(name = "Todo Controller")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @GetMapping
    public ResponseEntity<List<Todo>> getTodos() {
        return new ResponseEntity<>(todoService.getAllTodos(), HttpStatus.OK);
    }

    @PostMapping(
            path = "/save",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Todo> saveTodo(@RequestParam("title") String title,
                                         @RequestParam("description") String description,
                                         @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(todoService.saveTodo(title, description, file), HttpStatus.OK);
    }

    @GetMapping(value = "{id}/image/download")
    public byte[] downloadTodoImage(@PathVariable("id") Long id) throws Exception {
        byte[] source =  todoService.downloadTodoImage(id);
        return source;
    }

    @PostMapping(
            path = "/delete-object",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Boolean> deleteObject(@RequestBody ObjectRequest objectRequest) {
        return new ResponseEntity<>(todoService.deleteObject(objectRequest), HttpStatus.OK);
    }

    @PostMapping(
            path = "/upload-common",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Boolean> uploadCommon(@RequestParam("bucketName") String bucketName,
                                                @RequestParam(value = "path", required = false) String path,
                                                @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(todoService.uploadCommon(bucketName, path, file), HttpStatus.OK);
    }

    @GetMapping(path = "/s3-select-object")
    public ResponseEntity<?> s3SelectObject() throws Exception {
        return new ResponseEntity<>(todoService.s3SelectObject(), HttpStatus.OK);
    }

    /*private BufferedImage convert(byte[] imageSource) throws Exception {
        InputStream imageStream = new ByteArrayInputStream(imageSource);

        // read the image from the file
        BufferedImage image = ImageIO.read(imageStream);

        // create the object of ByteArrayOutputStream class
        ByteArrayOutputStream outStreamObj = new ByteArrayOutputStream();

        // write the image into the object of ByteArrayOutputStream class
        ImageIO.write(image, "jpg", outStreamObj);

        // create the byte array from image
        byte [] byteArray = outStreamObj.toByteArray();

        // create the object of ByteArrayInputStream class
        // and initialized it with the byte array.
        ByteArrayInputStream inStreambj = new ByteArrayInputStream(byteArray);

        // read image from byte array
        BufferedImage newImage = ImageIO.read(inStreambj);

        // write output image
        ImageIO.write(newImage, "jpg", new File("outputImage.jpg"));
        System.out.println("Image generated from the byte array.");
        return newImage;
    }*/
}
