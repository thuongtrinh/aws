package com.txt.aws.s3.controller;

import com.amazonaws.services.s3.model.Bucket;
import com.txt.aws.s3.dto.BucketRequest;
import com.txt.aws.s3.dto.ObjectRequest;
import com.txt.aws.s3.service.BucketService;
import com.txt.aws.s3.service.PreSignedURLService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/")
@AllArgsConstructor
@CrossOrigin("*")
@Tag(name = "Bucket Controller")
public class BucketController {
    private final BucketService bucketService;
    private final PreSignedURLService preSignedURLService;


    @GetMapping(
            path = "bucket/list-bucket",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<Bucket>> listBucket() {
        return new ResponseEntity<>(bucketService.listBucket(), HttpStatus.OK);
    }

    @PostMapping(
            path = "bucket/create-bucket",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Bucket> create(@RequestBody BucketRequest bucketName) {
        return new ResponseEntity<>(bucketService.createBucket(bucketName), HttpStatus.OK);
    }

    @PostMapping(
            path = "bucket/delete-bucket",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Boolean> delete(@RequestBody BucketRequest bucketRequest) {
        return new ResponseEntity<>(bucketService.deleteBucket(bucketRequest), HttpStatus.OK);
    }

    @PostMapping(
            path = "/presigned-url/uploadFile",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Boolean> presignedUploadFile(@RequestBody ObjectRequest bucketRequest) {
        return new ResponseEntity<>(preSignedURLService.presignedUploadFile(bucketRequest), HttpStatus.OK);
    }
}
