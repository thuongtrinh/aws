package com.txt.aws.s3.service;

import com.amazonaws.services.s3.model.Bucket;
import com.txt.aws.s3.dto.BucketRequest;

import java.util.List;

public interface BucketService {

    List<Bucket> listBucket();

    Bucket createBucket(BucketRequest bucketName);

    Boolean deleteBucket(BucketRequest bucketName);
}
