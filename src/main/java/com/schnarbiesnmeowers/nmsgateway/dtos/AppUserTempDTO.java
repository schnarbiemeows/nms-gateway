package com.schnarbiesnmeowers.nmsgateway.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.schnarbiesnmeowers.nmsgateway.entities.AppUserTemp;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author Dylan I. Kessler
 *
 */
public class AppUserTempDTO implements Serializable {
	// default serial version id, required for serializable classes
	private static final long serialVersionUID = 1L;

	//private static final Logger applicationLogger = LogManager.getLogger("FileAppender");

	/**
	 *
	 */
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private Integer userTempId;

	/**
	 *
	 */
	private String uniqueId;

	/**
	 *
	 */
	private List<String> authorizations;

	/**
	 *
	 */
	private String email;

	private String phone;

	private int age;

	/**
	 *
	 */
	private String firstName;

	/**
	 *
	 */
	private boolean actv;

	/**
	 *
	 */
	private boolean userNotLocked;

	/**
	 *
	 */
	private LocalDate joinDate;

	/**
	 *
	 */
	private LocalDate lastLoginDate;

	/**
	 *
	 */
	private LocalDate lastLoginDateDisplay;

	/**
	 *
	 */
	private String lastName;

	/**
	 *
	 */
	private String password;

	/**
	 *
	 */
	private String profileImage;

	/**
	 *
	 */
	private String roles;

	/**
	 *
	 */
	private String userIdentifier;

	/**
	 *
	 */
	private String userName;

	/**
	 *
	 */
	private LocalDate createdDate;

	/**
	 * default constructor
	 */
	public AppUserTempDTO() {
		super();
	}

	public AppUserTempDTO(Integer userTempId, String uniqueId, List<String> authorizations, String email,
						  String phone, int age, String firstName, boolean actv, boolean userNotLocked,
						  LocalDate joinDate, LocalDate lastLoginDate, LocalDate lastLoginDateDisplay,
						  String lastName, String password, String profileImage, String roles,
						  String userIdentifier, String userName, LocalDate createdDate) {
		this.userTempId = userTempId;
		this.uniqueId = uniqueId;
		this.authorizations = authorizations;
		this.email = email;
		this.phone = phone;
		this.age = age;
		this.firstName = firstName;
		this.actv = actv;
		this.userNotLocked = userNotLocked;
		this.joinDate = joinDate;
		this.lastLoginDate = lastLoginDate;
		this.lastLoginDateDisplay = lastLoginDateDisplay;
		this.lastName = lastName;
		this.password = password;
		this.profileImage = profileImage;
		this.roles = roles;
		this.userIdentifier = userIdentifier;
		this.userName = userName;
		this.createdDate = createdDate;
	}

	public Integer getUserTempId() {
		return userTempId;
	}

	public void setUserTempId(Integer userTempId) {
		this.userTempId = userTempId;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public List<String> getAuthorizations() {
		return authorizations;
	}

	public void setAuthorizations(List<String> authorizations) {
		this.authorizations = authorizations;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public boolean isActv() {
		return actv;
	}

	public void setActv(boolean actv) {
		this.actv = actv;
	}

	public boolean isUserNotLocked() {
		return userNotLocked;
	}

	public void setUserNotLocked(boolean userNotLocked) {
		this.userNotLocked = userNotLocked;
	}

	public LocalDate getJoinDate() {
		return joinDate;
	}

	public void setJoinDate(LocalDate joinDate) {
		this.joinDate = joinDate;
	}

	public LocalDate getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(LocalDate lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	public LocalDate getLastLoginDateDisplay() {
		return lastLoginDateDisplay;
	}

	public void setLastLoginDateDisplay(LocalDate lastLoginDateDisplay) {
		this.lastLoginDateDisplay = lastLoginDateDisplay;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getProfileImage() {
		return profileImage;
	}

	public void setProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}

	public String getRoles() {
		return roles;
	}

	public void setRoles(String roles) {
		this.roles = roles;
	}

	public String getUserIdentifier() {
		return userIdentifier;
	}

	public void setUserIdentifier(String userIdentifier) {
		this.userIdentifier = userIdentifier;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public LocalDate getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(LocalDate createdDate) {
		this.createdDate = createdDate;
	}

	@Override
	public String toString() {
		return "AppUserTempDTO{" +
				"userTempId=" + userTempId +
				", uniqueId='" + uniqueId + '\'' +
				", authorizations=" + authorizations +
				", email='" + email + '\'' +
				", phone='" + phone + '\'' +
				", age=" + age +
				", firstName='" + firstName + '\'' +
				", actv=" + actv +
				", userNotLocked=" + userNotLocked +
				", joinDate=" + joinDate +
				", lastLoginDate=" + lastLoginDate +
				", lastLoginDateDisplay=" + lastLoginDateDisplay +
				", lastName='" + lastName + '\'' +
				", password='" + password + '\'' +
				", profileImage='" + profileImage + '\'' +
				", roles='" + roles + '\'' +
				", userIdentifier='" + userIdentifier + '\'' +
				", userName='" + userName + '\'' +
				", createdDate=" + createdDate +
				'}';
	}

	public AppUserTemp toEntity() {
		return new AppUserTemp(this.getUserTempId(),this.getUniqueId(), this.getAuthorizations(),this.getEmail(),
				this.getPhone(),this.getAge(),this.getFirstName(),this.isActv(),this.isUserNotLocked(),
				this.getJoinDate(),this.getLastLoginDate(),this.getLastLoginDateDisplay(),this.getLastName(),
				this.getPassword(),this.getProfileImage(),this.getRoles(),this.getUserIdentifier(),this.getUserName(),
				this.getCreatedDate());
	}
}
