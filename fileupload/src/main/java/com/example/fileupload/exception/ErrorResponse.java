package com.example.fileupload.exception;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ErrorResponse{

	private LocalDateTime timestamp = LocalDateTime.now();

	private String exception;
	private String error;
	private String message;
	private Integer status;
	private List<ErrorMessage> errors = new ArrayList<>();
	private List<Map<String,Object>> contentErrors = new ArrayList<>();

	public ErrorResponse() {
	}
	public ErrorResponse(Integer status,String error, String message, List<ErrorMessage> errors,List<Map<String,Object>> contentErrors) {
		this.status = status;
		this.error = error;
		this.message = message;
		this.errors = errors;
		this.contentErrors=contentErrors;
	}


	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getException() {
		return exception;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public void setException(String exception) {
		this.exception = exception;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<ErrorMessage> getErrors() {
		return errors;
	}

	public void setErrors(List<ErrorMessage> errors) {
		this.errors = errors;
	}

	public List<Map<String, Object>> getContentErrors() {
		return contentErrors;
	}

	public void setContentErrors(List<Map<String, Object>> contentErrors) {
		this.contentErrors = contentErrors;
	}
}
