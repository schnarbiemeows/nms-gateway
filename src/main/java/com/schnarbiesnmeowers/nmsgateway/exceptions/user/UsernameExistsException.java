package com.schnarbiesnmeowers.nmsgateway.exceptions.user;

/**
 * 
 * @author Dylan I. Kessler
 *
 */
public class UsernameExistsException extends Exception {

	/**
	 * 
	 * @param message
	 */
	public UsernameExistsException(String message) {
		super(message);
	}
}
