package com.example.fileupload.rest;

import com.example.fileupload.exception.ErrorResponse;
import com.example.fileupload.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class FileUploadController {
	@Autowired
	FileUploadService fileUploadService;

	@RequestMapping(value = "/uploadMetaData", method = RequestMethod.POST, produces = {"text/csv","application/json"})
	public ErrorResponse metaDataFileUpload(@RequestParam(value="uploadFile", required=true) MultipartFile uploadFile) throws IOException {
	return fileUploadService.processFile(uploadFile);
	}
}
