package com.txt.aws.s3.controller;

import com.txt.aws.s3.entity.Todo;
import com.txt.aws.s3.service.TodoService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/v1/todo")
@AllArgsConstructor
@CrossOrigin("*")
public class TodoController {
    TodoService service;

    @GetMapping
    public ResponseEntity<List<Todo>> getTodos() {
        return new ResponseEntity<>(service.getAllTodos(), HttpStatus.OK);
    }

    @PostMapping(
            path = "/save",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Todo> saveTodo(@RequestParam("title") String title,
                                         @RequestParam("description") String description,
                                         @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(service.saveTodo(title, description, file), HttpStatus.OK);
    }

    @GetMapping(value = "{id}/image/download")
    public byte[] downloadTodoImage(@PathVariable("id") Long id) throws Exception {
        byte[] source =  service.downloadTodoImage(id);
        return source;
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
