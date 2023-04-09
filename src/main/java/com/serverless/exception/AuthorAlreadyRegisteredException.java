package com.serverless.exception;

public class AuthorAlreadyRegisteredException extends Exception {
    public AuthorAlreadyRegisteredException() {
        super("There is an author registered under given Identifier. Please check and resubmit");
    }
}
