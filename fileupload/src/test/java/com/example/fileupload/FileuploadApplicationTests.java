package com.example.fileupload;

import com.example.fileupload.exception.ErrorResponse;
import com.example.fileupload.exception.FileUploadException;
import com.example.fileupload.service.FileUploadService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = FileuploadApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class FileuploadApplicationTests {
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Autowired
	FileUploadService fileUploadService;

	@Test
	public void testUploadFileInvalidRowData() throws Exception {
		String csvData = "UserId,User,Role,Date_Created,Last_Modified_Date" + "\n" +
			"4234234,userOne1,Admin,09/08/2017,09/08/2017\n" +
			"4234235,userTwo,TESTING,09/08/2017,09/08/2017\n" +
			"4234236,userThree,DBA,21/08/2017,09/08/2017\n"+
			"4234237,userFour,DBA,09/08/2017,21/08/2017";
		ErrorResponse errorResponse = fileUploadService.processFile(getMultipartFileByString(csvData));
		assertThat(new ObjectMapper().writeValueAsString(errorResponse.getContentErrors()),is("[{\"rowNumber\":1,\"errors\":[{\"code\":\"INVALID_STRING_FORMAT\",\"description\":\"Invalid characters in User\",\"field\":\"User\",\"value\":\"userOne1\"}]},{\"rowNumber\":2,\"errors\":[{\"code\":\"INVALID_ROLE\",\"description\":\"Expected roles ADMIN,USER,DBA,ANALYST\",\"field\":\"Role\",\"value\":\"TESTING\"}]},{\"rowNumber\":3,\"errors\":[{\"code\":\"INVALID_INPUT\",\"description\":\"Invalid data format ,Expected MM/dd/yyyy\",\"field\":\"Date_Created\",\"value\":\"21/08/2017\"}]},{\"rowNumber\":4,\"errors\":[{\"code\":\"INVALID_INPUT\",\"description\":\"Invalid data format ,Expected MM/dd/yyyy\",\"field\":\"Last_Modified_Date\",\"value\":\"21/08/2017\"}]}]"));
		assertThat(errorResponse.getError(),is("ERRORS"));
	}

	@Test
	public void testUploadFileSuccess() throws Exception {
		String csvData = "UserId,User,Role,Date_Created,Last_Modified_Date" + "\n" +
			"4234234,userOne,Admin,09/08/2017,09/15/2017\n" +
			"4234235,userTwo,ANALYST,09/08/2017,09/08/2017\n" +
			"4234236,userThree,DBA,09/08/2017,09/08/2017\n"+
			"4234237,userFour,DBA,09/08/2017,09/08/2017";
		ErrorResponse errorResponse = fileUploadService.processFile(getMultipartFileByString(csvData));
		assertNull(errorResponse.getContentErrors());
		assertThat(errorResponse.getError(),is("SUCCESS"));
	}

	@Test
	public void testUploadFilePartialSuccess() throws Exception {
		String csvData = "UserId,User,Role,Date_Created,Last_Modified_Date" + "\n" +
			"4234234,userOne,Admin,09/08/2017,09/08/2017\n" +
			"4234235,userTwo,TESTING,09/08/2017,09/08/2017\n" +
			"4234236,userThree,DBA,21/08/2017,09/08/2017\n"+
			"4234237,userFour,DBA,09/08/2017,21/08/2017";
		ErrorResponse errorResponse = fileUploadService.processFile(getMultipartFileByString(csvData));
		assertThat(new ObjectMapper().writeValueAsString(errorResponse.getContentErrors()),is("[{\"rowNumber\":2,\"errors\":[{\"code\":\"INVALID_ROLE\",\"description\":\"Expected roles ADMIN,USER,DBA,ANALYST\",\"field\":\"Role\",\"value\":\"TESTING\"}]},{\"rowNumber\":3,\"errors\":[{\"code\":\"INVALID_INPUT\",\"description\":\"Invalid data format ,Expected MM/dd/yyyy\",\"field\":\"Date_Created\",\"value\":\"21/08/2017\"}]},{\"rowNumber\":4,\"errors\":[{\"code\":\"INVALID_INPUT\",\"description\":\"Invalid data format ,Expected MM/dd/yyyy\",\"field\":\"Last_Modified_Date\",\"value\":\"21/08/2017\"}]}]"));
		assertThat(errorResponse.getError(),is("PARTIAL_SUCCESS"));
	}



	private MockMultipartFile getMultipartFileByString(String csvData) {
		return new MockMultipartFile("file",
			"test.csv", "text/plain", csvData.getBytes());
	}


	@Test
	public void testUploadFileInvalidMissingHeader() throws Exception {
		expectedException.expect(FileUploadException.class);
		expectedException.expectMessage("Validation Errors");
		expectedException.expect(new ExceptionErrorMatches("[{\"code\":\"error\",\"description\":\"Missing headers\",\"field\":null,\"value\":null}]"));
		String csvData = "UserId,User,Role,Date_Created,TESTING" + "\n" +
			"4234234,userOne1,Admin,09/08/2017,09/08/2017\n" +
			"4234237,userFour,DBA,09/08/2017,21/08/2017";
		fileUploadService.processFile(getMultipartFileByString(csvData));
		}


	@Test
	public void testUploadFileInvalidTooManyHeaders() throws Exception {
		expectedException.expect(FileUploadException.class);
		expectedException.expectMessage("Validation Errors");
		expectedException.expect(new ExceptionErrorMatches("[{\"code\":\"error\",\"description\":\"Too Many headers\",\"field\":null,\"value\":null}]"));
		String csvData = "UserId,User,Role,Date_Created,Last_Modified_Date,TESTING" + "\n" +
			"4234234,userOne1,Admin,09/08/2017,09/08/2017\n" +
			"4234237,userFour,DBA,09/08/2017,21/08/2017";
		fileUploadService.processFile(getMultipartFileByString(csvData));
	}

	@Test
	public void testUploadFileInvalidFileSize() throws Exception {
		expectedException.expect(FileUploadException.class);
		expectedException.expectMessage("Validation Errors");
		expectedException.expect(new ExceptionErrorMatches("[{\"code\":\"error\",\"description\":\"File is Empty\",\"field\":null,\"value\":null}]"));
		String csvData = "";
		fileUploadService.processFile(getMultipartFileByString(csvData));
	}

	@Test
	public void testUploadFileInvalidFileHasNoRecords() throws Exception {
		expectedException.expect(FileUploadException.class);
		expectedException.expectMessage("Validation Errors");
		expectedException.expect(new ExceptionErrorMatches("[{\"code\":\"error\",\"description\":\"File contains no records\",\"field\":null,\"value\":null}]"));
		String csvData = "UserId,User,Role,Date_Created,Last_Modified_Date";
		fileUploadService.processFile(getMultipartFileByString(csvData));
	}


	class ExceptionErrorMatches extends TypeSafeMatcher<FileUploadException> {
		private String errors ="";

		public ExceptionErrorMatches(String errorString) {
			this.errors = errorString;
		}

		@Override
		protected boolean matchesSafely(FileUploadException fileUploadException) {
			try {
				return StringUtils.equalsIgnoreCase(errors,new ObjectMapper().writeValueAsString(fileUploadException.getErrors()));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		public void describeTo(Description description) {
				description.appendText("Expected Value ")
					.appendValue(errors);
		}

		@Override
		protected void describeMismatchSafely(FileUploadException fileUploadException, Description mismatchDescription) {
			try {
				mismatchDescription.appendText(" but was ")
		.appendValue(new ObjectMapper().writeValueAsString(fileUploadException.getErrors()));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}

		}
	}


}
