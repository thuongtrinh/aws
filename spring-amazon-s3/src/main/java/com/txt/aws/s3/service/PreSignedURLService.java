package com.txt.aws.s3.service;

import com.txt.aws.s3.dto.ObjectRequest;

public interface PreSignedURLService {
    Boolean presignedUploadFile(ObjectRequest bucketRequest);
}
