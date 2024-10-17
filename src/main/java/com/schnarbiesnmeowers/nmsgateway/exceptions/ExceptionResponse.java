package com.schnarbiesnmeowers.nmsgateway.exceptions;

import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.Date;

/**
 *
 * @author Dylan I. Kessler
 *
 */
public class ExceptionResponse extends ResponseEntityExceptionHandler {

	//private static final Logger applicationLogger = LogManager.getLogger("FileAppender");

	/**
	 * time the exception occurred
	 */
	private LocalDateTime timestamp;
	/**
	 * exception message
	 */
	private String message;
	/**
	 * detailed message
	 */
	private String details;

	/**
	 *
	 * @param timestamp
	 * @param message
	 * @param details
	 */
	public ExceptionResponse(LocalDateTime timestamp, String message, String details) {
		super();
		this.timestamp = timestamp;
		this.message = message;
		this.details = details;
	}

	/**
	 *
	 * @return Date
	 */
	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	/**
	 *
	 * @return String
	 */
	public String getMessage() {
		return message;
	}

	/**
	 *
	 * @return String
	 */
	public String getDetails() {
		return details;
	}
}
