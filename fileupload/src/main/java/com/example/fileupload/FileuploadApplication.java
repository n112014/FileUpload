package com.example.fileupload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(
	basePackageClasses = {FileuploadApplication.class}
)
public class FileuploadApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileuploadApplication.class, args);
	}
}
