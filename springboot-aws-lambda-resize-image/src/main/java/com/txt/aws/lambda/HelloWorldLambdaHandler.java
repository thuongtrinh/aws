package com.txt.aws.lambda;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class HelloWorldLambdaHandler implements RequestHandler<String, String> {
    public String handleRequest(String input, Context context) {
        System.out.println("Hello World! executed with input: " + input);
        return input;
    }
}
