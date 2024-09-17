package com.txt.aws.s3.controller;

import com.amazonaws.services.s3.model.Bucket;
import com.txt.aws.s3.dto.BucketRequest;
import com.txt.aws.s3.service.BucketService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/bucket")
@AllArgsConstructor
@CrossOrigin("*")
@Tag(name = "Bucket Controller")
public class BucketController {
    private final BucketService bucketService;

    @GetMapping(
            path = "/list-bucket",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<Bucket>> listBucket() {
        return new ResponseEntity<>(bucketService.listBucket(), HttpStatus.OK);
    }

    @PostMapping(
            path = "/create-bucket",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Bucket> create(@RequestBody BucketRequest bucketName) {
        return new ResponseEntity<>(bucketService.createBucket(bucketName), HttpStatus.OK);
    }

    @PostMapping(
            path = "/delete-bucket",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Boolean> delete(@RequestBody BucketRequest bucketRequest) {
        return new ResponseEntity<>(bucketService.deleteBucket(bucketRequest), HttpStatus.OK);
    }
}
