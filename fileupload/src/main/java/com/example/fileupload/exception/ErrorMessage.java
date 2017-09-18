package com.example.fileupload.exception;

public class ErrorMessage {


	private String code;

	private String description;

	private String field;

	private String value;

	public ErrorMessage(String code, String description) {
		this.code = code;
		this.description = description;
	}

	public ErrorMessage(String code, String description, String field) {
		this.code = code;
		this.description = description;
		this.field = field;
	}

	public ErrorMessage(String code, String description, String field ,String value) {
		this.code = code;
		this.description = description;
		this.field = field;
		this.value = value;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ErrorMessage that = (ErrorMessage) o;

		if (!code.equals(that.code)) return false;
		if (!description.equals(that.description)) return false;
		if (!value.equals(that.value)) return false;
		return field.equals(that.field);
	}

	@Override
	public int hashCode() {
		int result = code.hashCode();
		result = 31 * result + description.hashCode();
		result = 31 * result + field.hashCode();
		result = 31 * result + value.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "ErrorMessage{" +
			"code='" + code + '\'' +
			", description='" + description + '\'' +
			", field='" + field + '\'' +
			", value='" + value + '\'' +
			'}';
	}
}
