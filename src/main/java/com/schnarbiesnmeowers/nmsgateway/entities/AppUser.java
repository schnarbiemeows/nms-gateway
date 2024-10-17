package com.schnarbiesnmeowers.nmsgateway.entities;

import com.schnarbiesnmeowers.nmsgateway.dtos.AppUserDTO;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author Dylan I. Kessler
 *
 */
@Entity
@Table(name = "users")
public class AppUser implements Serializable {
	// default serial version id, required for serializable classes
	private static final long serialVersionUID = 1L;

	//private static final Logger applicationLogger = LogManager.getLogger("FileAppender");

	/**
	 *
	 */
	@Column(name = "user_id")
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer userId;

	/**
	 *
	 */
	@Column(name = "authorizations")
	private List<String> authorizations;

	/**
	 *
	 */
	@Column(name = "email")
	private String email;

	/**
	 *
	 */
	@Column(name = "first_name")
	private String firstName;

	/**
	 *
	 */
	@Column(name = "actv")
	private boolean actv;

	/**
	 *
	 */
	@Column(name = "user_not_locked")
	private boolean userNotLocked;

	/**
	 *
	 */
	@Column(name = "join_date")
	private LocalDate joinDate;


	@Column(name = "age")
	private int age;

	@Column(name = "phone")
	private String phone;
	/**
	 *
	 */
	@Column(name = "lst_logd_in")
	private LocalDate lastLoginDate;

	/**
	 *
	 */
	@Column(name = "last_login_date_display")
	private LocalDate lastLoginDateDisplay;

	/**
	 *
	 */
	@Column(name = "last_name")
	private String lastName;

	/**
	 *
	 */
	@Column(name = "password")
	private String password;

	/**
	 *
	 */
	@Column(name = "profile_image")
	private String profileImage;

	/**
	 *
	 */
	@Column(name = "roles")
	private String roles;

	/**
	 *
	 */
	@Column(name = "user_identifier")
	private String userIdentifier;

	/**
	 *
	 */
	@Column(name = "username")
	private String userName;

	/**
	 * default constructor
	 */
	public AppUser() {
		super();
	}

	public AppUser(Integer userId, List<String> authorizations, String email, String firstName, boolean actv,
				   boolean userNotLocked, LocalDate joinDate, int age, String phone, LocalDate lastLoginDate,
				   LocalDate lastLoginDateDisplay, String lastName, String password, String profileImage, String roles,
				   String userIdentifier, String userName) {
		this.userId = userId;
		this.authorizations = authorizations;
		this.email = email;
		this.firstName = firstName;
		this.actv = actv;
		this.userNotLocked = userNotLocked;
		this.joinDate = joinDate;
		this.age = age;
		this.phone = phone;
		this.lastLoginDate = lastLoginDate;
		this.lastLoginDateDisplay = lastLoginDateDisplay;
		this.lastName = lastName;
		this.password = password;
		this.profileImage = profileImage;
		this.roles = roles;
		this.userIdentifier = userIdentifier;
		this.userName = userName;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
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

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
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

	@Override
	public String toString() {
		return "AppUser{" +
				"userId=" + userId +
				", authorizations=" + authorizations +
				", email='" + email + '\'' +
				", firstName='" + firstName + '\'' +
				", actv=" + actv +
				", userNotLocked=" + userNotLocked +
				", joinDate=" + joinDate +
				", age=" + age +
				", phone='" + phone + '\'' +
				", lastLoginDate=" + lastLoginDate +
				", lastLoginDateDisplay=" + lastLoginDateDisplay +
				", lastName='" + lastName + '\'' +
				", password='" + password + '\'' +
				", profileImage='" + profileImage + '\'' +
				", roles='" + roles + '\'' +
				", userIdentifier='" + userIdentifier + '\'' +
				", userName='" + userName + '\'' +
				'}';
	}

	public AppUserDTO toDTO() {
		return new AppUserDTO(this.userId,this.authorizations,this.email,this.phone,this.age,this.firstName,
				this.actv,this.userNotLocked,this.joinDate,this.lastLoginDate,this.lastLoginDateDisplay,
				this.lastName,this.password,this.profileImage,this.roles,this.userIdentifier,this.userName);
	}
}
