package com.schnarbiesnmeowers.nmsgateway.dtos;

import java.time.LocalDate;

public class AppUserDTOWrapper extends AppUserDTO {

	String newEmailAddr;
	String newFirstName;
	String newLastName;
	String newPassword;
	String newUserName;
	public AppUserDTOWrapper() {
		super();
		// TODO Auto-generated constructor stub
	}
	public AppUserDTOWrapper(AppUserDTO record) {
		this.setAuthorizations(record.getAuthorizations());
		this.setEmail(record.getEmail());
		this.setFirstName(record.getFirstName());
		this.setJoinDate(record.getJoinDate());
		this.setLastLoginDate(record.getLastLoginDate());
		this.setLastLoginDateDisplay(record.getLastLoginDateDisplay());
		this.setLastName(record.getLastName());
		this.setPassword(record.getPassword());
		this.setProfileImage(record.getProfileImage());
		this.setRoles(record.getRoles());
		this.setActv(record.isActv());
		this.setUserNotLocked(record.isUserNotLocked());
		this.setUserIdentifier(record.getUserIdentifier());
		this.setUserName(record.getUserName());
	}

	public String getNewEmailAddr() {
		return newEmailAddr;
	}
	public void setNewEmailAddr(String newEmailAddr) {
		this.newEmailAddr = newEmailAddr;
	}
	public String getNewFirstName() {
		return newFirstName;
	}
	public void setNewFirstName(String newFirstName) {
		this.newFirstName = newFirstName;
	}
	public String getNewLastName() {
		return newLastName;
	}
	public void setNewLastName(String newLastName) {
		this.newLastName = newLastName;
	}
	public String getNewPassword() {
		return newPassword;
	}
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	public String getNewUserName() {
		return newUserName;
	}
	public void setNewUserName(String newUserName) {
		this.newUserName = newUserName;
	}
	
}
