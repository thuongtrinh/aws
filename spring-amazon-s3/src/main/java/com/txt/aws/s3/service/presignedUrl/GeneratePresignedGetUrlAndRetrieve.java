package com.txt.aws.s3.service.presignedUrl;

import com.txt.aws.s3.util.PresignUrlUtils;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.http.*;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.utils.IoUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.UUID;


@Slf4j
public class GeneratePresignedGetUrlAndRetrieve {
    private final static S3Client s3Client = S3Client.create();

    public static void main(String[] args) {
        String bucketName = "b-" + UUID.randomUUID();
        String keyName = "k-" + UUID.randomUUID();
        String resourcePath = "multipartUploadFiles/s3-userguide.pdf";

        PresignUrlUtils.createBucket(bucketName, s3Client);
        PresignUrlUtils.uploadFile(s3Client, bucketName, keyName, GeneratePresignedGetUrlAndRetrieve.getFileForForClasspathResource(resourcePath));

        GeneratePresignedGetUrlAndRetrieve presign = new GeneratePresignedGetUrlAndRetrieve();
        try {
            String presignedUrlString = presign.createPresignedGetUrl(bucketName, keyName);
            presign.useHttpUrlConnectionToGet(presignedUrlString);
            presign.useHttpClientToGet(presignedUrlString);
            presign.useSdkHttpClientToPut(presignedUrlString);
        } finally {
            PresignUrlUtils.deleteObject(bucketName, keyName, s3Client);
            PresignUrlUtils.deleteBucket(bucketName, s3Client);
        }
    }

    /* Create a pre-signed URL to download an object in a subsequent GET request. */
    public String createPresignedGetUrl(String bucketName, String keyName) {
        try (S3Presigner presigner = S3Presigner.create()) {

            GetObjectRequest objectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))  // The URL will expire in 10 minutes.
                    .getObjectRequest(objectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            log.info("Presigned URL: [{}]", presignedRequest.url().toString());
            log.info("HTTP method: [{}]", presignedRequest.httpRequest().method());

            return presignedRequest.url().toExternalForm();
        }
    }

    /* Use the JDK HttpURLConnection (since v1.1) class to do the download. */
    public byte[] useHttpUrlConnectionToGet(String presignedUrlString) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); // Capture the response body to a byte array.

        try {
            URL presignedUrl = new URL(presignedUrlString);
            HttpURLConnection connection = (HttpURLConnection) presignedUrl.openConnection();
            connection.setRequestMethod("GET");
            // Download the result of executing the request.
            try (InputStream content = connection.getInputStream()) {
                IoUtils.copy(content, byteArrayOutputStream);
            }
            log.info("HTTP response code is " + connection.getResponseCode());

        } catch (S3Exception | IOException e) {
            log.error(e.getMessage(), e);
        }
        return byteArrayOutputStream.toByteArray();
    }
    // snippet-end:[presigned.java2.generatepresignedgeturlandretrieve.basichttpclient]

    // snippet-start:[presigned.java2.generatepresignedgeturlandretrieve.jdkhttpclient]
    /* Use the JDK HttpClient (since v11) class to do the download. */
    public byte[] useHttpClientToGet(String presignedUrlString) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); // Capture the response body to a byte array.

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        HttpClient httpClient = HttpClient.newHttpClient();
        try {
            URL presignedUrl = new URL(presignedUrlString);
            HttpResponse<InputStream> response = httpClient.send(requestBuilder
                            .uri(presignedUrl.toURI())
                            .GET()
                            .build(),
                    HttpResponse.BodyHandlers.ofInputStream());

            IoUtils.copy(response.body(), byteArrayOutputStream);

            log.info("HTTP response code is " + response.statusCode());

        } catch (URISyntaxException | InterruptedException | IOException e) {
            log.error(e.getMessage(), e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    /* Use the AWS SDK for Java SdkHttpClient class to do the download. */
    public byte[] useSdkHttpClientToPut(String presignedUrlString) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); // Capture the response body to a byte array.
        try {
            URL presignedUrl = new URL(presignedUrlString);
            SdkHttpRequest request = SdkHttpRequest.builder()
                    .method(SdkHttpMethod.GET)
                    .uri(presignedUrl.toURI())
                    .build();

            HttpExecuteRequest executeRequest = HttpExecuteRequest.builder()
                    .request(request)
                    .build();

            try (SdkHttpClient sdkHttpClient = ApacheHttpClient.create()) {
                HttpExecuteResponse response = sdkHttpClient.prepareRequest(executeRequest).call();
                response.responseBody().ifPresentOrElse(
                        abortableInputStream -> {
                            try {
                                IoUtils.copy(abortableInputStream, byteArrayOutputStream);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        () -> log.error("No response body."));

                log.info("HTTP Response code is {}", response.httpResponse().statusCode());
            }
        } catch (URISyntaxException | IOException e) {
            log.error(e.getMessage(), e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static File getFileForForClasspathResource(String resourcePath) {
        try {
            URL resource = GeneratePresignedGetUrlAndRetrieve.class.getClassLoader().getResource(resourcePath);
            return Paths.get(resource.toURI()).toFile();
        } catch (URISyntaxException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
