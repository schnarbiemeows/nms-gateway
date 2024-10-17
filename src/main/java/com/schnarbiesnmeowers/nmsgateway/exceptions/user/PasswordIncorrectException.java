package com.schnarbiesnmeowers.nmsgateway.exceptions.user;

/**
 * when the user tries to change their password, but their old password is
 * incorrect, this is the exception that gets thrown
 * @author Dylan I. Kessler
 *
 */
public class PasswordIncorrectException extends Exception {

	/**
	 * 
	 * @param message
	 */
	public PasswordIncorrectException(String message) {
		super(message);
	}
}
