package com.txt.aws.s3.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.txt.aws.s3.dto.ObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileStore {

    private final AmazonS3 amazonS3;

    public void upload(String path,
                       String fileName,
                       Optional<Map<String, String>> optionalMetaData,
                       InputStream inputStream) {

        ObjectMetadata objectMetadata = new ObjectMetadata();
        optionalMetaData.ifPresent(map -> {
            if (!map.isEmpty()) {
                map.forEach(objectMetadata::addUserMetadata);
            }
        });
        try {
            amazonS3.putObject(path, fileName, inputStream, objectMetadata);
        } catch (AmazonServiceException e) {
            throw new IllegalStateException("Failed to upload the file", e);
        }
    }

    public byte[] download(String path, String key) {
        try {
            S3Object object = amazonS3.getObject(path, key);
            S3ObjectInputStream objectContent = object.getObjectContent();
            return IOUtils.toByteArray(objectContent);
        } catch (AmazonServiceException | IOException e) {
            throw new IllegalStateException("Failed to download the file", e);
        }
    }

    public Boolean deleteObject(ObjectRequest objectRequest) {
        String bucket_name = objectRequest.getBucketName();
        String object_key = objectRequest.getObjectName();
        System.out.format("Deleting object %s from S3 bucket: %s\n", object_key, bucket_name);
        // final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.DEFAULT_REGION).build();
        try {
            amazonS3.deleteObject(bucket_name, object_key);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            //System.exit(1);
            return false;
        }
        System.out.println("Done!");
        return true;
    }
}
