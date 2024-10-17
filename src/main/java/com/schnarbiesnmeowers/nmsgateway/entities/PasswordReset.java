package com.schnarbiesnmeowers.nmsgateway.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Date;

/**
 *
 * @author Dylan I. Kessler
 *
 */
@Entity
@Table(name = "password_reset")
public class PasswordReset {

	// default serial version id, required for serializable classes
	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 */
	@Column(name = "password_reset_id")
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer passwordResetId;

	/**
	 * 
	 */
	@Column(name = "unique_id")
	private String uniqueId;
	
	/**
	 * 
	 */
	@Column(name = "email_addr")
	private String emailAddr;
	
	/**
	 * 
	 */
	@Column(name = "created_date")
	private LocalDate createdDate;

	public PasswordReset() {
		super();
		// TODO Auto-generated constructor stub
	}

	public PasswordReset(Integer passwordResetId, String uniqueId, String emailAddr, LocalDate createdDate) {
		super();
		this.passwordResetId = passwordResetId;
		this.uniqueId = uniqueId;
		this.emailAddr = emailAddr;
		this.createdDate = createdDate;
	}

	public Integer getPasswordResetId() {
		return passwordResetId;
	}

	public void setPasswordResetId(Integer passwordResetId) {
		this.passwordResetId = passwordResetId;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getEmailAddr() {
		return emailAddr;
	}

	public void setEmailAddr(String emailAddr) {
		this.emailAddr = emailAddr;
	}

	public LocalDate getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(LocalDate createdDate) {
		this.createdDate = createdDate;
	}
	
	
}
