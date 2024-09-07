package com.txt.aws.s3.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BucketName {
    TODO_IMAGE("bucket-second");
    private final String bucketName;
}
