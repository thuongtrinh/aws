package com.txt.aws.s3.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.txt.aws.s3.service.S3SelectService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class S3SelectServiceImpl implements S3SelectService {

    private final AmazonS3 amazonS3;

    private static final String QUERY = "select s.name from S3Object s";

    @Value("${aws-config.s3.bucket-select}")
    private String BUCKET_NAME;

    @Value("${aws-config.s3.csv-object-key}")
    private String CSV_OBJECT_KEY;

    @Value("${aws-config.s3.select-results-path}")
    private String S3_SELECT_RESULTS_PATH;

    @Override
    public void amazonS3SelectObject(String fileName) throws Exception {
        recordInputStream(fileName);
    }

    private void recordInputStream(String fileName) throws Exception {
//        final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();

        SelectObjectContentRequest request = generateBaseCSVRequest(BUCKET_NAME, CSV_OBJECT_KEY, QUERY);
        final AtomicBoolean isResultComplete = new AtomicBoolean(false);

        try (OutputStream fileOutputStream = new FileOutputStream(new File(S3_SELECT_RESULTS_PATH));
             SelectObjectContentResult result = amazonS3.selectObjectContent(request)) {
            InputStream resultInputStream = result.getPayload().getRecordsInputStream(
                    new SelectObjectContentEventVisitor() {
                        @Override
                        public void visit(SelectObjectContentEvent.StatsEvent event) {
                            System.out.println("Received Stats, Bytes Scanned: " + event.getDetails().getBytesScanned()
                                    + " Bytes Processed: " + event.getDetails().getBytesProcessed());
                        }

                        /*
                         * An End Event informs that the request has finished successfully.
                         */
                        @Override
                        public void visit(SelectObjectContentEvent.EndEvent event) {
                            isResultComplete.set(true);
                            System.out.println("Received End Event. Result is complete.");
                        }
                    }
            );

            IOUtils.copy(resultInputStream, fileOutputStream);
        }

        /*
         * The End Event indicates all matching records have been transmitted.
         * If the End Event is not received, the results may be incomplete.
         */
        if (!isResultComplete.get()) {
            throw new Exception("S3 Select request was incomplete as End Event was not received.");
        }
    }

    private static SelectObjectContentRequest generateBaseCSVRequest(String bucket, String key, String query) {
        SelectObjectContentRequest request = new SelectObjectContentRequest();
        request.setBucketName(bucket);
        request.setKey(key);
        request.setExpression(query);
        request.setExpressionType(ExpressionType.SQL);

        InputSerialization inputSerialization = new InputSerialization();
        inputSerialization.setCsv(new CSVInput());
        inputSerialization.setCompressionType(CompressionType.NONE);
        request.setInputSerialization(inputSerialization);

        OutputSerialization outputSerialization = new OutputSerialization();
        outputSerialization.setCsv(new CSVOutput());
        request.setOutputSerialization(outputSerialization);

        return request;
    }
}
