package com.schnarbiesnmeowers.nmsgateway.exceptions.user;

/**
 * 
 * @author Dylan I. Kessler
 *
 */
public class ExpiredLinkException extends Exception {

	/**
	 * 
	 * @param message
	 */
	public ExpiredLinkException(String message) {
		super(message);
	}
}
