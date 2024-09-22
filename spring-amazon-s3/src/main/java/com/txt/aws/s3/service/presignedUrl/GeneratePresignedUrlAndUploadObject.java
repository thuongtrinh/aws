package com.txt.aws.s3.service.presignedUrl;

import com.txt.aws.s3.util.PresignUrlUtils;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

@Slf4j
public class GeneratePresignedUrlAndUploadObject {

    public static void main(String[] args) {
        String bucketName = "b-" + UUID.randomUUID();
        String keyName = "k-" + UUID.randomUUID();

        try (S3Client s3Client = S3Client.create()) {
            PresignUrlUtils.createBucket(bucketName, s3Client);
            GeneratePresignedUrlAndUploadObject presignAndPut = new GeneratePresignedUrlAndUploadObject();
            try {
                URL presignedUrl = presignAndPut.createSignedUrlForStringPut(bucketName, keyName);
                presignAndPut.useHttpUrlConnectionToPutString(presignedUrl);
                presignAndPut.useHttpClientToPutString(presignedUrl);
            } finally {
                PresignUrlUtils.deleteObject(bucketName, keyName, s3Client);
                PresignUrlUtils.deleteBucket(bucketName, s3Client);
            }
        }
    }

    /**
     * Create a presigned URL for uploading a String object.
     *
     * @param bucketName - The name of the bucket.
     * @param keyName    - The name of the object.
     * @return - The presigned URL for an HTTP PUT.
     */
    public URL createSignedUrlForStringPut(String bucketName, String keyName) {
        try (S3Presigner presigner = S3Presigner.create()) {

            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .contentType("text/plain")
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))  // The URL will expire in 10 minutes.
                    .putObjectRequest(objectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
            String myURL = presignedRequest.url().toString();
            log.info("Presigned URL to upload to: [{}]", myURL);
            log.info("Which HTTP method needs to be used when uploading: [{}]", presignedRequest.httpRequest().method());

            return presignedRequest.url();
        }
    }

    /**
     * Use the JDK HttpURLConnection (since v1.1) class to upload a String, but you can
     * use any HTTP client.
     *
     * @param presignedUrl - The presigned URL.
     */
    public void useHttpUrlConnectionToPutString(URL presignedUrl) {
        try {
            // Create the connection and use it to upload the new object by using the presigned URL.
            HttpURLConnection connection = (HttpURLConnection) presignedUrl.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "text/plain");
            connection.setRequestMethod("PUT");
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write("This text was uploaded as an object by using a presigned URL.");
            out.close();

            connection.getResponseCode();
            log.info("HTTP response code is " + connection.getResponseCode());

        } catch (S3Exception | IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Use the JDK HttpClient class (since v11) to upload a String,
     * but you can use any HTTP client.
     *
     * @param presignedUrl - The presigned URL.
     */
    public void useHttpClientToPutString(URL presignedUrl) {
        HttpClient httpClient = HttpClient.newHttpClient();
        try {
            final HttpResponse<Void> response = httpClient.send(HttpRequest.newBuilder()
                            .uri(presignedUrl.toURI())
                            .header("Content-Type", "text/plain")
                            .PUT(HttpRequest.BodyPublishers.ofString("This text was uploaded as an object by using a presigned URL."))
                            .build(),
                    HttpResponse.BodyHandlers.discarding());
            log.info("HTTP response code is " + response.statusCode());
        } catch (S3Exception | IOException | URISyntaxException | InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }
}
