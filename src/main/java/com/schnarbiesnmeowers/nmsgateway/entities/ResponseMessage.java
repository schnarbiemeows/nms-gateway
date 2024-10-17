package com.schnarbiesnmeowers.nmsgateway.entities;

import java.io.Serializable;

public class ResponseMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	private String message = "";

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ResponseMessage(String message) {
		super();
		this.message = message;
	}

	public ResponseMessage() {
		super();
	}
	
	
}