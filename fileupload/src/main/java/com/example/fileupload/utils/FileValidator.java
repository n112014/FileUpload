package com.example.fileupload.utils;

import com.example.fileupload.exception.ErrorMessage;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.RegexValidator;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import static java.util.Arrays.asList;

@Component
public class FileValidator {
	public static final String[] EXPECTED_HEADER = new String[]{"UserId", "User", "Role", "Date_Created", "Last_Modified_Date"};
	protected static final RegexValidator REGEX_VALIDATOR_USER = new RegexValidator("^[A-Za-z\\s]{1,50}");
	protected static final RegexValidator REGEX_VALIDATOR_USER_ID = new RegexValidator("^[0-9].*");
	public static final DateTimeFormatter FORMATTER_MM_DD_YYYY = DateTimeFormatter.ofPattern("M/d/yyyy");


	private static final Set<String> ROLES = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

	static {
		ROLES.addAll(asList("ADMIN", "USER", "DBA", "ANALYST"));
	}


	public List<ErrorMessage> validateFile(MultipartFile uploadFile) {
		List<ErrorMessage> errors = new ArrayList<>();
		if (CollectionUtils.isEmpty(validateSizeAndName(uploadFile, errors))) {
			validateHeaders(uploadFile, errors);
		}
		return errors;
	}

	private List<ErrorMessage> validateSizeAndName(MultipartFile uploadFile, List<ErrorMessage> errors) {
		if (uploadFile.getSize() == 0) {
			errors.add(new ErrorMessage("error", "File is Empty"));
		}
		if (!uploadFile.getOriginalFilename().toLowerCase().endsWith(".csv")) {
			errors.add(new ErrorMessage("error", "File is not csv"));
		}
		if (stringContainInvalidChars(uploadFile.getOriginalFilename())) {
			errors.add(new ErrorMessage("error", "File Name contains invalid characters , File name should contains only alphanumerics"));
		}
		return errors;
	}

	private void validateHeaders(MultipartFile uploadFile, List<ErrorMessage> errors) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(uploadFile.getInputStream()))) {
			List<String> actualHeaders = getHeaders(reader);
			if (actualHeaders.containsAll(Arrays.asList(EXPECTED_HEADER))) {
				if (actualHeaders.size() > EXPECTED_HEADER.length) {
					errors.add(new ErrorMessage("error", "Too Many headers"));
				} else {//match the Order of Header
					if (!(StringUtils.equalsIgnoreCase(actualHeaders.get(0), EXPECTED_HEADER[0]) && StringUtils.equalsIgnoreCase(actualHeaders.get(1), EXPECTED_HEADER[1])
						&& StringUtils.equalsIgnoreCase(actualHeaders.get(2), EXPECTED_HEADER[2]) && StringUtils.equalsIgnoreCase(actualHeaders.get(3), EXPECTED_HEADER[3])
						&& StringUtils.equalsIgnoreCase(actualHeaders.get(4), EXPECTED_HEADER[4]))) {
						errors.add(new ErrorMessage("error", "Order of headers doesn't match with expected headers(\"USER_ID\",\"USER\", \"ROLE\", \"DATE_CREATED\", \"LAST_MODIFIED_DATE\")"));
					}
				}
			} else {
				errors.add(new ErrorMessage("error", "Missing headers"));
			}
			//validate for no of rows
			Integer numRows = Math.toIntExact(reader.lines().count());
			if (numRows == 0) {
				errors.add(new ErrorMessage("error", "File contains no records"));
			}
		} catch (IOException e) {
			errors.add(new ErrorMessage("error", "invalidFile"));
		}
	}

	private static List<String> getHeaders(BufferedReader bufferedReader) {
		return bufferedReader.lines().findFirst().map(line -> Arrays.asList(line.split(","))).get();
	}


	private static boolean stringContainInvalidChars(String fileName) {
		Set<String> invalidChars = new HashSet<>(Arrays.asList(" ", "/", ":", "*", "\"", "<", ">", "|"));
		for (String s : fileName.split("(?!^)")) {
			if (invalidChars.contains(s)) {
				return true;
			}
		}
		return false;
	}


	public List<ErrorMessage> validateRequestData(Map<String, String> requestData) {
		List<ErrorMessage> errorMessages = new ArrayList<>();

        /*required field check*/
		CollectionUtils.addIgnoreNull(errorMessages, fieldPresentCheck(EXPECTED_HEADER[0], requestData.get(EXPECTED_HEADER[0])));
		CollectionUtils.addIgnoreNull(errorMessages, fieldPresentCheck(EXPECTED_HEADER[1], requestData.get(EXPECTED_HEADER[1])));
		CollectionUtils.addIgnoreNull(errorMessages, fieldPresentCheck(EXPECTED_HEADER[2], requestData.get(EXPECTED_HEADER[2])));
		CollectionUtils.addIgnoreNull(errorMessages, fieldPresentCheck(EXPECTED_HEADER[3], requestData.get(EXPECTED_HEADER[3])));

		if (!errorMessages.isEmpty()) {
			return errorMessages;
		}

        /*format validation*/
		CollectionUtils.addIgnoreNull(errorMessages, userIdformatCheck(requestData.get(EXPECTED_HEADER[0])));
		CollectionUtils.addIgnoreNull(errorMessages, userFormatCheck(requestData.get(EXPECTED_HEADER[1])));
		CollectionUtils.addIgnoreNull(errorMessages, validateRole(requestData.get(EXPECTED_HEADER[2])));
		CollectionUtils.addIgnoreNull(errorMessages, dateCheck(requestData.get(EXPECTED_HEADER[3]), EXPECTED_HEADER[3]));
		if (!Objects.isNull(requestData.get(EXPECTED_HEADER[4])))
			CollectionUtils.addIgnoreNull(errorMessages, dateCheck(requestData.get(EXPECTED_HEADER[4]), EXPECTED_HEADER[4]));
		return errorMessages;
	}

	private ErrorMessage fieldPresentCheck(String name, Object value) {
		boolean invalid = false;
		if (Objects.isNull(value) || (value instanceof String && StringUtils.isBlank((String) value))) {
			invalid = true;
		}
		if (invalid) {
			return new ErrorMessage("INVALID_INPUT", "Missing required field.", name);
		} else {
			return null;
		}
	}


	private ErrorMessage dateCheck(String date, String name) {
		try {
			LocalDate data = LocalDate.parse(date, FORMATTER_MM_DD_YYYY);
		} catch (DateTimeParseException e) {
			return new ErrorMessage("INVALID_INPUT", "Invalid data format ,Expected MM/dd/yyyy", name,date);
		}
		return null;
	}


	private ErrorMessage validateRole(String role) {
		return ROLES.contains(role) ? null : new ErrorMessage("INVALID_ROLE", "Expected roles ADMIN,USER,DBA,ANALYST", "Role",role);
	}

	private ErrorMessage userFormatCheck(String value) {
		return REGEX_VALIDATOR_USER.isValid(value) ? null : new ErrorMessage("INVALID_STRING_FORMAT", "Invalid characters in User", "User",value);
	}

	private ErrorMessage userIdformatCheck(String value) {
		return REGEX_VALIDATOR_USER_ID.isValid(value) ? null : new ErrorMessage("INVALID_NUMBER_FORMAT", "invalid Characters", "UserId",value);
	}

}
