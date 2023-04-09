package com.serverless.exception;

public class IncomingRequestParsingException extends RuntimeException {
    public IncomingRequestParsingException() {
        super("Error in processing incoming request");
    }
}
