package com.txt.aws.s3.dto;

import lombok.Data;

@Data
public class ObjectRequest {
    private String objectName;
    private String bucketName;
}
