package com.schnarbiesnmeowers.nmsgateway.services;

import com.schnarbiesnmeowers.nmsgateway.dtos.CheckPasswordResetResponseDTO;
import com.schnarbiesnmeowers.nmsgateway.dtos.AppUserDTO;
import com.schnarbiesnmeowers.nmsgateway.dtos.AppUserDTOWrapper;
import com.schnarbiesnmeowers.nmsgateway.dtos.PasswordResetDTO;
import com.schnarbiesnmeowers.nmsgateway.entities.AppUser;
import com.schnarbiesnmeowers.nmsgateway.entities.AppUserTemp;
import com.schnarbiesnmeowers.nmsgateway.entities.PasswordReset;
import com.schnarbiesnmeowers.nmsgateway.exceptions.user.*;
import com.schnarbiesnmeowers.nmsgateway.repositories.AppUserRepository;
import com.schnarbiesnmeowers.nmsgateway.repositories.AppUserTempRepository;
import com.schnarbiesnmeowers.nmsgateway.repositories.PasswordResetRepository;
import com.schnarbiesnmeowers.nmsgateway.security.UserPrincipal;
import com.schnarbiesnmeowers.nmsgateway.utilities.Roles;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.SendFailedException;
import javax.mail.internet.AddressException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;

import static com.schnarbiesnmeowers.nmsgateway.utilities.Constants.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.springframework.http.MediaType.*;

/**
 * 
 * @author Dylan I. Kessler
 *
 */
@Service
@Transactional
@Qualifier("UserDetailsService")
public class UserServiceImpl implements UserService, ReactiveUserDetailsService {

	private static final Logger applicationLogger = LogManager.getLogger("FileAppender");
	private static final Logger emailLogger = LogManager.getLogger("EmailAppender");
	
	private AppUserRepository appUserRepository;
	private AppUserTempRepository appUserTempRepository;
	private PasswordEncoder passwordEncoder;
	private LoginAttemptService loginAttemptService;
	private PasswordResetRepository passwordResetRepository;
	private EmailService emailService;
	
	@Value("${success.email.expiration.minutes}")
	private int linkExpirationTime;
	
	/**
	 * constructor using constructor injection
	 * @param appUserRepository
	 * @param appUserTempRepository
	 * @param passwordEncoder
	 * @param loginAttemptService
	 * @param emailService
	 */
	@Autowired
	public UserServiceImpl(AppUserRepository appUserRepository,
                           AppUserTempRepository appUserTempRepository,
						   PasswordEncoder passwordEncoder,
                           LoginAttemptService loginAttemptService,
                           PasswordResetRepository passwordResetRepository,
                           EmailService emailService ) {
		super();
		this.appUserRepository = appUserRepository;
		this.appUserTempRepository = appUserTempRepository;
		this.passwordEncoder = passwordEncoder;
		this.loginAttemptService = loginAttemptService;
		this.passwordResetRepository = passwordResetRepository;
		this.emailService = emailService;
	}

	/**
	 * this is the main method that the Spring Security will call; our program itself does not directly call it
	 * @param username
	 * @returns UserDetails
	 */
	@Override
	public Mono<UserDetails> findByUsername(String username) throws UsernameNotFoundException {
		logAction("finding User - " + username);
		Mono<AppUser> user = appUserRepository.findUserByUserName(username);
		if(user == null) {
			logAction("User - " + username + " not found!");
			throw new UsernameNotFoundException(USER_NOT_FOUND + " : " + username);
		} else {
			try {
				validateLoginAttempt(user);
			} catch (AddressException e) {
				logAction("AddressException when trying to send an email to administrators for a locked account");
				e.printStackTrace();
			} catch (MessagingException e) {
				logAction("MessagingException when trying to send an email to administrators for a locked account");
				e.printStackTrace();
			}
			user = user.map(rec -> {
				rec.setLastLoginDateDisplay(rec.getLastLoginDate());
				return rec;
			});
			user = user.map(rec -> {
				rec.setLastLoginDate(LocalDate.now())
				; return rec;
			});
			appUserRepository.save(user.block());
			UserPrincipal userPrincipal = new UserPrincipal(user.map(rec -> rec.toDTO()).block());
			logAction("returning User - " + username);
			return userPrincipal;
		}
	}


	/**
	 * this method will validate the login attempt and send management an email if the account gets locked
	 * @param user
	 * @throws MessagingException 
	 * @throws AddressException 
	 */
	private void validateLoginAttempt(Mono<AppUser> user) throws AddressException, MessagingException {
		logAction("validateLoginAttempt for User - " + user.getUserName());
		if(user.isUserNotLocked()) {
			if(this.loginAttemptService.hasExceededMaxAttempts(user.getUserName())) {
				logAction("locking User - " + user.getUserName());
				user.setUserNotLocked(false);
				this.emailService.sendManagementEmail("App Program - Locked Account","the account for username = " + user.getUserName() + " was locked");
			} else {
				logAction("unlocking User - " + user.getUserName());
				user.setUserNotLocked(true);
			}
		} else {
			logAction("User - " + user.getUserName() + " is locked");
			this.loginAttemptService.evictUserFromLoginCache(user.getUserName());
		}
	}

	/**
	 * this is the registration main method
	 * it will put a record into the interview_user_temp table instead of the normal interview_user table
	 * the user needs to confirm their email in order for this record to get transferred over to the normal table.
	 * @param firstName
	 * @param lastName
	 * @param username
	 * @param email
	 * @return
	 * @throws UserNotFoundException
	 * @throws UsernameExistsException
	 * @throws EmailExistsException
	 * @throws AddressException
	 * @throws MessagingException
	 */
	@Override
	public Mono<AppUser> register(String firstName, String lastName, String username, String email, String password) throws UserNotFoundException, UsernameExistsException, EmailExistsException, AddressException, MessagingException {
		logAction("inside register; validating username and email");
		validateNewUsernameAndEmail(StringUtils.EMPTY,username,email);
		logAction("username and email validation passed");
		AppUser user = new AppUser();
		AppUserTemp tempUser = new AppUserTemp();
		String uniqueId = generateUniqueId();
		String userIdentifier = generateUserIdentifier();
		String encodedPassword = encodePassword(password);
		
		tempUser.setUniqueId(uniqueId);
		tempUser.setUserIdentifier(userIdentifier);
		tempUser.setFirstName(firstName);
		tempUser.setLastName(lastName);
		tempUser.setUserName(username);
		tempUser.setEmail(email);
		tempUser.setJoinDate(LocalDate.now());
		tempUser.setCreatedDate(LocalDate.now());
		tempUser.setPassword(encodedPassword);
		tempUser.setActv(true);
		tempUser.setUserNotLocked(true);
		tempUser.setRoles(Roles.ROLE_BASIC_USER.name());
		tempUser.setAuthorizations(Roles.ROLE_BASIC_USER.getAuthorizations());
		tempUser.setProfileImage(getTemporaryImageUrl(username));
		
		user.setUserIdentifier(userIdentifier);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setUserName(username);
		user.setEmail(email);
		user.setJoinDate(LocalDate.now());
		user.setActv(true);
		user.setUserNotLocked(true);
		user.setRoles(Roles.ROLE_BASIC_USER.name());
		user.setAuthorizations(Roles.ROLE_BASIC_USER.getAuthorizations());
		user.setProfileImage(getTemporaryImageUrl(username));
		appUserTempRepository.save(tempUser);
		logAction("New user identifier = " + userIdentifier);
		logEmailAction("sending the confirm email for a new registrant");
		this.emailService.sendConfirmEmailEmail(email,uniqueId);
		return Mono.just(user);
	}
	
	/**
	 * this method gets called when the user confirms their email address, and will copy over the
	 * user's interview_user_temp record into the interview_user table, and then delte that record.
	 * @param id
	 * @return
	 * @throws ExpiredLinkException 
	 * @throws UserNotFoundException 
	 */
	@Override
	public Mono<AppUser> confirmEmail(String id) throws ExpiredLinkException, UserNotFoundException {
		logEmailAction("inside the confirmEmail method after the user click the confirm email link");
		logEmailAction("id = " + id);
		Mono<AppUserTemp> tempUser = appUserTempRepository.findUserByUniqueId(id);
		tempUser.hasElement().flatMap(rec -> {
			if(rec) {
				if(isTheRecordExpired(tempUser.getCreatedDate())) {
					logEmailAction("record found in interview_user_temp, but is EXPIRED for id = " + id);
					appUserTempRepository.delete(tempUser.b);
					throw new ExpiredLinkException(EXPIRED_LINK);
				}
				logEmailAction("record found in interview_user_temp, copying over for id = " + id);
				AppUser newUser = copyOverFromTempRecord(tempUser);
				appUserRepository.save(newUser);
				appUserTempRepository.delete(tempUser.);
				logEmailAction("leaving the confirmEmail method");
				return newUser;
			} else {
				logEmailAction("not record found in interview_user_temp for id = " + id);
				throw new UserNotFoundException(NO_USER_FOUND_BY_ID);
			}
		});
	}
	
	/**
	 * method to determine if the temp record is expired
	 * the default time = 5 minutes
	 * @param recordDate
	 * @return
	 */
	private Mono<Boolean> isTheRecordExpired(Mono<LocalDate> recordDate) {
		recordDate.map(rec -> {
			if(Duration.between(rec, LocalDate.now()).getSeconds()*1000 > linkExpirationTime*60000) {
				logEmailAction("difference = " + Duration.between(rec, LocalDate.now()).getSeconds()*1000);
				return true;
			} else {
				logEmailAction("difference = " + Duration.between(rec, LocalDate.now()).getSeconds()*1000);
				return false;
			}
		});
		return Mono.just(false);
	}
	
	/**
	 * this method will copy over the values from the temporary record into a permanent AppUser record
	 * @param tempUser
	 * @return
	 */
	private Mono<AppUser> copyOverFromTempRecord(Mono<AppUserTemp> tempUser) {
		Mono<AppUser> newUser = tempUser.map( rec ->
				new AppUser(null,rec.getAuthorizations(),rec.getEmail(),
						rec.getFirstName(),rec.isActv(),rec.isUserNotLocked(),
						rec.getJoinDate(),rec.getAge(),rec.getPhone(),rec.getLastLoginDate(),
						rec.getLastLoginDateDisplay(),
						rec.getLastName(),rec.getPassword(),rec.getProfileImage(),
						rec.getRoles(),rec.getUserIdentifier(),rec.getUserName()));
		return newUser;
	}

	/**
	 * TEMPORARY method to manually set passwords to what I want them to be - for testing only
	 *
	 * @param username
	 * @param password
	 * @return
	 */
	 public Mono<Void> setPassword(String username, String password) {
		logAction("New user identifier = " + username + " to --> " + password);
	 	Mono<AppUser> user = appUserRepository.findUserByUserName(username);
	 	String encodedPassword = encodePassword(password);
	 	user = user.map(rec -> {
			 rec.setPassword(encodedPassword);
			return rec;
		});
	 	user.map( rec -> appUserRepository.save(rec));
		return null;
	 }
	 
	/**
	 * this method set's the role and authorizations on the interview_user record
	 *
	 * @param username
	 * @return
	 */
	 public Mono<Void> setRole(String username) {
		Mono<AppUser> user = appUserRepository.findUserByUserName(username);
		user = user.map(rec -> {
			rec.setRoles(Roles.ROLE_SUPER.name());
			rec.setAuthorizations(Roles.ROLE_SUPER.getAuthorizations());
			return rec;
		});
		 user.map(rec -> appUserRepository.save(rec));
		 return null;
	 }
	 
	
	/**
	 * this method will retrieve a temporary image for the user's profile
	 * @param username
	 * @return
	 */
	public String getTemporaryImageUrl(String username) {
		return null;
		// this will later need to be some generic image either in the resources folder, or in S3
		//return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PATH + username).toUriString();
	}

	/**
	 * this method will encode a new user's password
	 * @param password
	 * @return
	 */
	public String encodePassword(String password) {
		return this.passwordEncoder.encode(password);
	}

	/**
	 * this method generates a random password for a new user
	 * @return
	 */
	private String generateUniqueId() {
		return RandomStringUtils.randomAlphanumeric(255);
	}

	/**
	 * this method creates a random userIdentifier for a Basic User(BU)
	 * it first checks to make sure there is not already a user with that identifier
	 * @return
	 */
	public String generateUserIdentifier() {
		/**
		 * I need to figure out how to do this correctly, maybe just make the ID # large enough that
		 * a collision can't happen?
		 */
		boolean validUserIdentifier = false;
		do {
			String userIdentifier = "BU" + RandomStringUtils.randomNumeric(10);
			Mono<AppUser> user = appUserRepository.findUserByUserIdentifier(userIdentifier);
			Mono<Boolean> hasAlready = user.hasElement().flatMap(rec -> {
				if(rec) {
					return
				} else {
					validUserIdentifier = true;
				}
			})
			if(user==null) {
				validUserIdentifier = true;
			} else {
				userIdentifier = "BU" ;
			}
		} while(validUserIdentifier == false);
		return userIdentifier; 
	}

	/**
	 * this method checks that a new user is not registering with a username or email that already exists, and or, for a user who is changing their username,
	 * the old username exists
	 * @param currentUsername
	 * @param newUsername
	 * @param newEmail
	 * @return AppUser
	 * @throws UserNotFoundException
	 * @throws UsernameExistsException
	 * @throws EmailExistsException
	 */
	public Mono<AppUser> validateNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail) throws UserNotFoundException, UsernameExistsException, EmailExistsException {
        if(StringUtils.isNotBlank(currentUsername)) {
        	// this is not a new person trying to register
        	Mono<AppUser> currentUser = findUserByUsername(currentUsername);
        	if(currentUser == null) {
        		logAction("currentUser is null");
        		throw new UserNotFoundException(NO_USER_FOUND_BY_USERNAME + currentUsername);
        	}
			Mono<AppUser> newUser = findUserByUsername(newUsername);
        	if(newUser != null && !newUser.getUserId().equals(currentUser.getUserId())) {
        		logAction("login: username is already taken");
        		throw new UsernameExistsException(USERNAME_ALREADY_EXISTS);
        	}
			Mono<AppUser> userByEmail = findUserByEmail(newEmail);
        	logAction("email address is already taken");
        	if(userByEmail != null && !userByEmail.getUserId().equals(currentUser.getUserId())) {
        		throw new EmailExistsException(A_USER_WITH_THIS_EMAIL_ALREADY_EXISTS);
        	}
        	return currentUser;
        } else {
        	// this is a new person trying to register
			Mono<AppUser> newUser = findUserByUsername(newUsername);
        	if(newUser != null) {
        		logAction("registration: username is already taken");
        		throw new UsernameExistsException(USERNAME_ALREADY_EXISTS);
        	}
			Mono<AppUser> userByEmail = findUserByEmail(newEmail);
        	if(userByEmail != null) {
        		logAction("registration: email address is already taken");
        		throw new EmailExistsException(A_USER_WITH_THIS_EMAIL_ALREADY_EXISTS);
        	}
        	return null;
        }
    }
	
	/**
	 * method to get all users
	 * @ return List<AppUser>
	 */
	@Override
	public Flux<AppUser> getAllUsers() {
		return appUserRepository.findAll();
	}

	/**
	 * method to find a list of users/admins by role
	 * @param role
	 * @return Flux<AppUser>
	 */
	@Override
	public Flux<AppUser> getUsersByRole(String role) {
		String roles = "'" + role + "'";
		return appUserRepository.findByRoleTypes(roles);
	}

	/**
	 * method to get just users
	 * @return Flux<AppUser>
	 */
	@Override
	public Flux<AppUser> getJustUsers() {
		String roles = "'ROLE_BASIC_USER','ROLE_ADV_USER','ROLE_PREMIUM_USER'";
		return appUserRepository.findByRoleTypes(roles);
	}

	/**
	 * method to get just admins
	 * @return
	 */
	@Override
	public Flux<AppUser> getAdmins() {
		String roles = "'ROLE_ADMIN','ROLE_SUPER'";
		return appUserRepository.findByRoleTypes(roles);
	}

	/**
	 * find the user in the interview_user table using the username field
	 * @param username
	 * @return
	 */
	@Override
	public Mono<AppUser> findUserByUsername(String username) {
		return appUserRepository.findUserByUserName(username);
	}

	/**
	 * find the user in the interview_user table using the emailAddr field
	 * @param email
	 * @return
	 */
	@Override
	public Mono<AppUser> findUserByEmail(String email) {
		return appUserRepository.findUserByEmail(email);
	}

	/**
	 * this method will check to see if the randomly generated userIdentifier already exists in the database
	 * @param userIdentifier
	 * @return
	 */
	private Mono<AppUser> findUserByUserIdentifier(String userIdentifier) {
		return appUserRepository.findUserByUserIdentifier(userIdentifier);
	}
	/**
	 * this is an Admin method to add a new user to the interview_user table.
	 * This method is only to be used by administrators
	 * @param firstName
	 * @param lastName
	 * @param username
	 * @param email
	 * @param role
	 * @param isNotLocked
	 * @param isActive
	 * @param profileImage
	 * @return
	 * @throws UserNotFoundException
	 * @throws UsernameExistsException
	 * @throws EmailExistsException
	 * @throws IOException
	 * @throws NotAnImageFileException
	 */
	@Override
	public Mono<AppUser> addNewUser(String firstName, String lastName, String username, String email, String role,
			boolean isNotLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageFileException {
		validateNewUsernameAndEmail(StringUtils.EMPTY,username,email);
		AppUser user = new AppUser();
		String userIdentifier = generateUserIdentifier();
		user.setUserIdentifier(userIdentifier);
		String password = generateUniqueId();
		String encodedPassword = encodePassword(password);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setUserName(username);
		user.setEmail(email);
		user.setJoinDate(LocalDate.now());
		user.setPassword(encodedPassword);
		user.setActv(isActive);
		user.setUserNotLocked(isNotLocked);
		user.setRoles(getRoleEnumName(role).name());
		user.setAuthorizations(getRoleEnumName(role).getAuthorizations());
		user.setProfileImage(getTemporaryImageUrl(username));
		appUserRepository.save(user);
		saveProfileImage(user, profileImage);
		return Mono.just(user);
	}

	/**
	 * this is an Admin method to update an interview_user record.
	 * This method is only to be used by administrators
	 * @return
	 * @throws UserNotFoundException
	 * @throws UsernameExistsException
	 * @throws EmailExistsException
	 * @throws IOException
	 */
	@Override
	public Mono<AppUser> updateUserByUser(AppUserDTOWrapper userInput) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, PasswordIncorrectException {
		/*
		 * I am doing this below JUST IN CASE any of the "new" fields might be null; we don't want to accidently
		 * wipe any of these fields in the database
		 * also, this allows us flexibility on the front-end if we want to have pages/sections
		 * where we are just updating specific fields
		 */
		String newUsername = userInput.getUserName();
		String newEmail = userInput.getEmail();
		String newFirstName = userInput.getFirstName();
		String newLastName = userInput.getLastName();
		if(userInput.getNewUserName()!=null&&!userInput.getNewUserName().isEmpty()) {
			newUsername = userInput.getNewUserName();
		}
		if(userInput.getNewEmailAddr()!=null&&!userInput.getNewEmailAddr().isEmpty()) {
			newEmail = userInput.getNewEmailAddr();
		}
		if(userInput.getNewFirstName()!=null&&!userInput.getNewFirstName().isEmpty()) {
			newFirstName = userInput.getNewFirstName();
		}
		if(userInput.getNewLastName()!=null&&!userInput.getNewLastName().isEmpty()) {
			newLastName = userInput.getNewLastName();
		}
		Mono<AppUser> user = validateNewUsernameAndEmail(userInput.getUserName(),newUsername,newEmail);
		if(userInput.getNewPassword()!=null) {
			String encodedPassword = encodePassword(userInput.getNewPassword());
			/*String oldPasswordUserEntered = encodePassword(userInput.getPassword());
			String oldPasswordFromDB = user.getPassword();
			if(!oldPasswordFromDB.equals(oldPasswordUserEntered)) {
				throw new PasswordIncorrectException(INCORRECT_OLD_PASSWORD);
			}*/
			user = user.map(rec -> {
				rec.setPassword(encodedPassword);
				return rec;
			});
		}
		String finalNewEmail = newEmail;
		String finalNewUsername = newUsername;
		String finalNewFirstName = newFirstName;
		String finalNewLastName = newLastName;
		return user.map(rec -> {
			rec.setEmail(finalNewEmail);
			rec.setUserName(finalNewUsername);
			rec.setFirstName(finalNewFirstName);
			rec.setLastName(finalNewLastName);
			appUserRepository.save(rec);
			return rec;
		});
	}

	/**
	 * this is an Admin method to delete an interview_user record.
	 * This method is only to be used by administrators
	 * @param username
	 * @throws IOException
	 */
	@Override
	public Mono<Void> deleteUser(String username) throws IOException {
		Mono<AppUser> user = this.appUserRepository.findUserByUserName(username);
		user.map(rec -> {
			deleteDir(new File(Paths.get(USER_FOLDER +
					rec.getUserName()).toAbsolutePath().normalize().toString()));
					return rec;
		}).map(rec -> appUserRepository.deleteById(rec.getUserId()));
        return null;
    }

	void deleteDir(File file) {
		File[] contents = file.listFiles();
		if (contents != null) {
			for (File f : contents) {
				deleteDir(f);
			}
		}
		file.delete();
	}

	/**
	 * this method will initiate a user's password reset, and send them an email with a link for them to click to
	 * reset their password
	 * if no account with that email is found, a different email will be sent to the address informing them of this
	 * 
	 * @param email
	 * @throws AddressException
	 * @throws MessagingException
	 * @throws EmailNotFoundException
	 */
	@Override
	public Mono<Void> resetPasswordInitiation(String email) throws AddressException, MessagingException, EmailNotFoundException {
		// first check to see if there is a record in interview_user for that email
		Mono<AppUser> user = this.findUserByEmail(email);
		if(user == null) {
			// if not, send the NoAddressFoundPREmailTemplate email
			this.emailService.sendNoAddressFoundEmail(email, true);
		}
		else {
			// check to see if there is already a record in password_reset
			String uniqueId = generateUniqueId();
			LocalDate today = LocalDate.now();
			PasswordReset passwordResetRecord = this.passwordResetRepository.findUserByEmailAddr(email);
			if(passwordResetRecord == null) {
				passwordResetRecord = new PasswordReset(null,uniqueId,email,today);
			} else {
				passwordResetRecord.setUniqueId(uniqueId);
				passwordResetRecord.setCreatedDate(today);
			}
			this.passwordResetRepository.save(passwordResetRecord);
			this.emailService.sendForgotPasswordEmail(email,uniqueId);
		}
	}

	/**
	 * this method will send the user an email with their username in it
	 * @param email
	 * @throws AddressException
	 * @throws MessagingException
	 * @throws EmailNotFoundException
	 */
	@Override
	public Mono<Void> forgotUsername(String email) throws AddressException, MessagingException, EmailNotFoundException {
		Mono<AppUser> user = this.findUserByEmail(email);
		if(user == null) {
			// if not, send the NoAddressFoundPREmailTemplate email
			this.emailService.sendNoAddressFoundEmail(email, false);
		} else {
			this.emailService.sendEmailWithUsername(email,user.getUserName());
		}	
	}
	
	
	/**
	 * method to update a user's profile image
	 * @param username
	 * @param profileImage
	 * @return
	 * @throws UserNotFoundException
	 * @throws UsernameExistsException
	 * @throws EmailExistsException
	 * @throws IOException
	 * @throws NotAnImageFileException
	 */
	@Override
	public Mono<AppUser> updateProfileImage(String username, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageFileException {
		AppUser user = validateNewUsernameAndEmail(username, null, null);
		saveProfileImage(user, profileImage);
		return user;
	}

	/**
	 * method to save a user's profile image to storage
	 * @param user
	 * @param profileImage
	 * @throws IOException
	 * @throws NotAnImageFileException
	 */
	private void saveProfileImage(AppUser user, MultipartFile profileImage) throws IOException, NotAnImageFileException {
		if (profileImage != null) {
            if(!Arrays.asList(IMAGE_JPEG_VALUE, IMAGE_PNG_VALUE, IMAGE_GIF_VALUE).contains(profileImage.getContentType())) {
                throw new NotAnImageFileException(profileImage.getOriginalFilename() + NOT_AN_IMAGE_FILE);
            }
            Path userFolder = Paths.get(USER_FOLDER + user.getUserName()).toAbsolutePath().normalize();
            if(!Files.exists(userFolder)) {
                Files.createDirectories(userFolder);
                logAction(DIRECTORY_CREATED + userFolder);
            }
            Files.deleteIfExists(Paths.get(userFolder + user.getUserName() + DOT + JPG_EXTENSION));
            Files.copy(profileImage.getInputStream(), userFolder.resolve(user.getUserName() + DOT + JPG_EXTENSION), REPLACE_EXISTING);
            user.setProfileImage(setProfileImageUrl(user.getUserName()));
            this.appUserRepository.save(user);
            logAction(FILE_SAVED_IN_FILE_SYSTEM + profileImage.getOriginalFilename());
        }
	}

	/**
	 * method to set the user's profileImage URL
	 * @param username
	 * @return
	 */
	private String setProfileImageUrl(String username) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(USER_IMAGE_PATH + username + FORWARD_SLASH
        + username + DOT + JPG_EXTENSION).toUriString();
    }
	
	/**
	 * this method gets the value of the Role using the key
	 * @param role
	 * @return
	 */
	private Roles getRoleEnumName(String role) {
		return Roles.valueOf(role.toUpperCase());
	}

	/**
	 * test method for testing the email functionality
	 * TODO - remove
	 * @throws Exception 
	 * @throws MessagingException 
	 * @throws AddressException 
	 */
	@Override
	public void testEmail() throws AddressException, MessagingException, Exception {
		this.emailService.testEmail();
	}

	/**
	 * this method checks the password_reset table first to see if there actually is a password reset request record
	 * associated with the given unique Id
	 * @param id
	 * @return
	 * @throws MessagingException 
	 * @throws SendFailedException 
	 * @throws NoSuchProviderException 
	 * @throws AddressException 
	 */
	@Override
	public CheckPasswordResetResponseDTO checkPasswordResetTable(String id) throws AddressException, NoSuchProviderException, SendFailedException, MessagingException {
		logBothAction("inside checkPasswordResetTable for id = " + id);
		CheckPasswordResetResponseDTO  results = null;
		PasswordReset resetRecord = passwordResetRepository.findUserByUniqueId(id);
		if(resetRecord == null) {
			logBothAction("no record found in password_reset for id = " + id);
			this.emailService.sendManagementEmail("App Program: checkPasswordResetTable issue", "A check on the password_reset table for the unique id = " + id + " has failed!");
			results = new CheckPasswordResetResponseDTO(false,null,null);
		} else {
			String emailAdddress = resetRecord.getEmailAddr();
			// check to see if the timestamp has expired
			boolean expiredRecord = isTheRecordExpired(resetRecord.getCreatedDate());
			if(expiredRecord) {
				logBothAction("record found in password_reset, but is EXPIRED for id = " + id);
				passwordResetRepository.delete(resetRecord);
				results = new CheckPasswordResetResponseDTO(false,emailAdddress,null);
			} else {
				logBothAction("record found in password_reset for id = " + id);
				// if a valid record is found, change the unique ID, and send back (true,emailAddress,newUniqueId)
				String newUniqueId = generateUserIdentifier();
				resetRecord.setUniqueId(newUniqueId);
				logBothAction("changing the unique Id to : " + newUniqueId);
				passwordResetRepository.save(resetRecord);
				results = new CheckPasswordResetResponseDTO(true,emailAdddress,newUniqueId);
			}
		}
		logBothAction("leaving checkPasswordResetTable for id = " + id);
		return results;
	}

	/**
	 * this method is for the scenario where a customer had a rest password email sent to them, but then decided to try
	 * to login anyways, and was successful.
	 * This would lead to a hanging password_reset record, so we need to find it and delete it.
	 * @param email
	 */
	@Override
	public Mono<Void> checkThePasswordResetTable(String email) {
		Mono<PasswordReset> resetRecord = passwordResetRepository.findUserByEmailAddr(email);
		if(null!=resetRecord) {
			passwordResetRepository.delete(resetRecord.block());
		}
	}

	/**
	 * method to actually change the user's password
	 * @param input
	 * @return
	 * @throws AddressException
	 * @throws NoSuchProviderException
	 * @throws SendFailedException
	 * @throws MessagingException
	 * @throws PasswordResetException
	 */
	@Override
	public AppUserDTO changePassword(PasswordResetDTO input) throws AddressException, NoSuchProviderException, SendFailedException, MessagingException, PasswordResetException {
		logAction("inside changePassword, changing the password for unique Id = " + input.getUniqueId());
		PasswordReset resetRecord = passwordResetRepository.findUserByUniqueId(input.getUniqueId());
		if(resetRecord == null) {
			logAction("inside changePassword, no record was found for unique Id = " + input.getUniqueId());
			this.emailService.sendManagementEmail("App Program: checkPasswordResetTable issue", "A final check on the password_reset table for the unique id = " + input.getUniqueId() + " has failed, the record was not found!");
			throw new PasswordResetException("We're sorry, but there was an issue with your request");
		}
		if(!resetRecord.getEmailAddr().equals(input.getEmailAddress())) {
			logAction("inside changePassword, email address did not match the one found in the record for unique Id = " + input.getUniqueId());
			this.emailService.sendManagementEmail("App Program: checkPasswordResetTable issue", "A final check on the password_reset table for the unique id = " + input.getUniqueId() + " has failed, the email addresses did not match!");
			throw new PasswordResetException("We're sorry, but there was an issue with your request");
		}
		logAction("inside changePassword, password_reset record was found unique Id = " + input.getUniqueId());
		AppUser user = appUserRepository.findUserByEmail(input.getEmailAddress());
		logAction("inside changePassword, user : " + user.getUserName() + " was retrieved, changing their password");
		String encodedPassword = encodePassword(input.getPassword());
		user.setPassword(encodedPassword);
		appUserRepository.save(user);
		passwordResetRepository.delete(resetRecord);
		logAction("leaving changePassword, user : " + user.getUserName());
		return user.toDTO();
	}
	
	/**
	 * logger method
	 * @param message
	 */
	private static void logBothAction(String message) {
		System.out.println("UserServiceImpl: " + message);
		applicationLogger.debug("UserServiceImpl: " + message);
		emailLogger.debug("UserServiceImpl: " + message);
	}
	/**
	 * logger method
	 * @param message
	 */
	private static void logAction(String message) {
    	System.out.println("UserServiceImpl: " + message);
    	applicationLogger.debug("UserServiceImpl: " + message);
    }
	
	/**
	 * logging method
	 * @param message
	 */
	private static void logEmailAction(String message) {
		System.out.println("UserServiceImpl: " + message);
		emailLogger.debug("UserServiceImpl: " + message);
	}

}
