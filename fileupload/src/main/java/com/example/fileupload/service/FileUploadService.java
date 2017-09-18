package com.example.fileupload.service;

import com.example.fileupload.exception.ErrorMessage;
import com.example.fileupload.exception.ErrorResponse;
import com.example.fileupload.exception.FileUploadException;
import com.example.fileupload.utils.FileValidator;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;

@Service
public class FileUploadService {
	@Value("${fileupload.metaDataDirectory}")
	private String metaDataDirectory;

	private static final String[] EXPECTED_HEADER = new String[]{"UserId", "User", "Role", "Date_Created", "Last_Modified_Date"};

	private final FileValidator fileValidator;

	@Autowired
	public FileUploadService(FileValidator fileValidator) {
		this.fileValidator = fileValidator;
	}

	@PostConstruct
	public void prepareDirectories() throws IOException {
		Files.createDirectories(Paths.get(metaDataDirectory));

        /*Fail the context booting if directories are not accessible*/
		if (!isDirectoryExist(metaDataDirectory)) {
			throw new IOException("Cannot access directories.");
		}
	}


	private boolean isDirectoryExist(String directory) {
		return Paths.get(directory).toFile().exists();
	}

	public ErrorResponse processFile(MultipartFile file) {
		List<ErrorMessage> errors = fileValidator.validateFile(file);
		if (!CollectionUtils.isEmpty(errors)) {
			throw new FileUploadException(400, "BAD_REQUEST", "Validation Errors", errors);
		}

		//create metaDataFile
		File metaDataFile = new File(metaDataDirectory+"metaData.csv");

		//process the content of the file if each row is valid
		AtomicInteger rowNumber = new AtomicInteger();
		Map<String, String> requestData = new HashMap<>();
		List<Map<String,Object>> contentErrors = new ArrayList<>();
 		try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()), ',', '\0', 1);
			 CSVWriter metaDataCsvWriter = createMetaDataFileHeader(metaDataFile)) {
			String[] row;
			while ((row = reader.readNext()) != null) {
				rowNumber.getAndIncrement();
				requestData.clear();
				final String[] rowValues = row;
				requestData = IntStream.range(0, rowValues.length).boxed().collect(toMap(i -> EXPECTED_HEADER[i], i -> StringUtils.strip(rowValues[i], "\"")));
				//Validate the content of each row , if row data is valid it is saved to FileSystem , else added to error Response
				List<ErrorMessage> errorMessageList = fileValidator.validateRequestData(requestData);
				if (!CollectionUtils.isEmpty(errorMessageList)) {
					Map<String,Object> rowError = new HashMap<>();
					rowError.put("rowNumber",rowNumber.get());
					rowError.put("errors",errorMessageList);
					contentErrors.add(rowError);
				} else {
					//save it to fileSystem
					metaDataCsvWriter.writeNext(rowValues);
					metaDataCsvWriter.flush();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(CollectionUtils.isEmpty(contentErrors)){
 			return new ErrorResponse(200,"SUCCESS","Successfully Processed All Rows",null,null);
		}else {
			return new ErrorResponse(200,rowNumber.get()-contentErrors.size()>0?"PARTIAL_SUCCESS":"ERRORS",contentErrors.size() +" Rows Failed.",null,contentErrors);
		}
	}

	private CSVWriter createMetaDataFileHeader(File metaDataFile) throws IOException {
		CSVWriter writer = new CSVWriter(new FileWriter(metaDataFile, true), ',');
		/*only add header for empty file*/
		if (metaDataFile.length() == 0) {
			writer.writeNext(new String[]{"UserId", "User", "Role", "Date_Created", "Last_Modified_Date"});
		}
		return writer;
	}

	@PreDestroy
	public void deleteDirectories() throws IOException {
		Files.deleteIfExists(Paths.get(metaDataDirectory));
	}
}
