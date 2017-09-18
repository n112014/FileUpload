package com.example.fileupload.rest;

import com.example.fileupload.exception.ErrorResponse;
import com.example.fileupload.exception.FileUploadException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;

@RestControllerAdvice
public class FileUploadControllerAdvice {
    @ExceptionHandler(FileUploadException.class)
    public ErrorResponse exception(FileUploadException e, HttpServletResponse response) {
        response.setStatus(e.getStatus());
        return new ErrorResponse(e.getStatus(),e.getError(), e.getMessage(), e.getErrors(),null);
    }
}
