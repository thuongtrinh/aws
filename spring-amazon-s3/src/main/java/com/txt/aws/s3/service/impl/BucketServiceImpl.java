package com.txt.aws.s3.service.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.txt.aws.s3.dto.BucketRequest;
import com.txt.aws.s3.service.BucketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BucketServiceImpl implements BucketService {

    private final AmazonS3 amazonS3;

    @Override
    public List<Bucket> listBucket() {
//        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.DEFAULT_REGION).build();
        List<Bucket> buckets = amazonS3.listBuckets();
        return buckets;
    }

    @Override
    public Bucket createBucket(BucketRequest bucketRequest) {
//        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.DEFAULT_REGION).build();
        Bucket b = null;
        String bucketName = bucketRequest.getBucketName();
        if (amazonS3.doesBucketExistV2(bucketName)) {
            System.out.format("Bucket %s already exists.\n", bucketName);
            b = getBucket(bucketName);
        } else {
            try {
                b = amazonS3.createBucket(bucketName);
            } catch (AmazonS3Exception e) {
                System.err.println(e.getErrorMessage());
            }
        }
        return b;
    }

    private Bucket getBucket(String bucketName) {
        Bucket namedBucket = null;
        List<Bucket> buckets = listBucket();
        for (Bucket b : buckets) {
            if (b.getName().equals(bucketName)) {
                namedBucket = b;
            }
        }
        return namedBucket;
    }

    @Override
    public Boolean deleteBucket(BucketRequest bucketRequest) {
        String bucketName = bucketRequest.getBucketName();
        System.out.println("Deleting S3 bucket: " + bucketName);
//        final AmazonS3 amazonS3 = AmazonS3ClientBuilder.standard().withRegion(Regions.DEFAULT_REGION).build();
        try {
            System.out.println(" - removing objects from bucket");
            ObjectListing object_listing = amazonS3.listObjects(bucketName);
            while (true) {
                for (Iterator<?> iterator = object_listing.getObjectSummaries().iterator(); iterator.hasNext(); ) {
                    S3ObjectSummary summary = (S3ObjectSummary) iterator.next();
                    amazonS3.deleteObject(bucketName, summary.getKey());
                }

                // more object_listing to retrieve?
                if (object_listing.isTruncated()) {
                    object_listing = amazonS3.listNextBatchOfObjects(object_listing);
                } else {
                    break;
                }
            }

            System.out.println(" - removing versions from bucket");
            VersionListing version_listing = amazonS3.listVersions(new ListVersionsRequest().withBucketName(bucketName));
            while (true) {
                for (Iterator<?> iterator = version_listing.getVersionSummaries().iterator(); iterator.hasNext(); ) {
                    S3VersionSummary vs = (S3VersionSummary) iterator.next();
                    amazonS3.deleteVersion(bucketName, vs.getKey(), vs.getVersionId());
                }

                if (version_listing.isTruncated()) {
                    version_listing = amazonS3.listNextBatchOfVersions(version_listing);
                } else {
                    break;
                }
            }

            System.out.println(" OK, bucket ready to delete!");
            amazonS3.deleteBucket(bucketName);
        } catch (AmazonServiceException e) {
            e.printStackTrace();
            System.err.println(e.getErrorMessage());
//            System.exit(1);
            return false;
        }
        System.out.println("Done!");
        return true;
    }

}
