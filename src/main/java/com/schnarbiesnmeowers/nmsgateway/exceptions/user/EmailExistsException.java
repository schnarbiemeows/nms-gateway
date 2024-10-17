package com.schnarbiesnmeowers.nmsgateway.exceptions.user;

/**
 * 
 * @author Dylan I. Kessler
 *
 */
public class EmailExistsException extends Exception {

	/**
	 * 
	 * @param message
	 */
	public EmailExistsException(String message) {
		super(message);
	}
}
