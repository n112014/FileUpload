package com.example.fileupload.exception;

import java.util.List;

public class FileUploadException extends RuntimeException{
    private final List<ErrorMessage> errors;

    private int status;

    private String error;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public FileUploadException(int status, String error, String message, List<ErrorMessage> errors) {
        super(message);
        this.errors = errors;
        this.status=status;
        this.error=error;
    }

    public List<ErrorMessage> getErrors() {
        return errors;
    }
}
