package com.schnarbiesnmeowers.nmsgateway.controllers;

import com.schnarbiesnmeowers.nmsgateway.dtos.*;
import com.schnarbiesnmeowers.nmsgateway.entities.AppUser;
import com.schnarbiesnmeowers.nmsgateway.entities.ResponseMessage;
import com.schnarbiesnmeowers.nmsgateway.exceptions.handler.AppUserExceptionHandling;
import com.schnarbiesnmeowers.nmsgateway.exceptions.handler.HttpResponse;
import com.schnarbiesnmeowers.nmsgateway.exceptions.user.*;
import com.schnarbiesnmeowers.nmsgateway.security.JwtTokenProvider;
import com.schnarbiesnmeowers.nmsgateway.security.UserPrincipal;
import com.schnarbiesnmeowers.nmsgateway.services.UserBusinessService;
import com.schnarbiesnmeowers.nmsgateway.services.UserService;
import com.schnarbiesnmeowers.nmsgateway.utilities.Constants;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.SendFailedException;
import javax.mail.internet.AddressException;
import java.text.ParseException;
import java.util.List;

import static com.schnarbiesnmeowers.nmsgateway.utilities.Constants.INCORRECT_OLD_PASSWORD;
import static com.schnarbiesnmeowers.nmsgateway.utilities.Constants.JWT_TOKEN_HEADER;

/**
 * this class is the main REST controller
 * 
 * @author Dylan I. Kessler
 *
 */
//@CrossOrigin
@RestController
@RequestMapping(path = "/user")
public class UserController extends AppUserExceptionHandling {

	private static final Logger applicationLogger = LogManager.getLogger("FileAppender");
	private static final Logger emailLogger = LogManager.getLogger("EmailAppender");
	public static final String EMAIL_SENT = "An email with a new password was sent to: ";
	public static final String USER_DELETED_SUCCESSFULLY = "User deleted successfully";

	private UserBusinessService userBusinessService;;
	private UserService userService;
	private AuthenticationManager authManager;
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	public UserController(UserBusinessService userBusinessService, UserService userService,
						  AuthenticationManager authManager, JwtTokenProvider jwtTokenProvider) {
		super();
		this.userBusinessService = userBusinessService;
		this.userService = userService;
		this.authManager = authManager;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	/**
	 * TODO - there are a number of things wrong with this method: 1 - makes no
	 * check to see if the fields are null or blank; will put record in DB 2 - makes
	 * no email validation; will put record with invalid email in the DB, then fails
	 * when sending the email
	 */
	/**
	 * this method allows the user to register an account
	 * 
	 * @param user
	 * @return ResponseEntity<AppUserDTO>
	 * @throws UserNotFoundException
	 * @throws UsernameExistsException
	 * @throws EmailExistsException
	 * @throws AddressException
	 * @throws MessagingException
	 * @throws UserFieldsNotValidException
	 */
	@PostMapping(path = "/register")
	public ResponseEntity<AppUserDTO> register(@RequestBody AppUserTempDTO user)
			throws UserNotFoundException, UsernameExistsException, EmailExistsException,
			AddressException,
			MessagingException, UserFieldsNotValidException {
		logAction("attempting to validate new user registration");
		userBusinessService.validateFields(user);
		logAction("new user registration validated");
		AppUser newUser = userService.register(user.getFirstName(), user.getLastName(), user.getUserName(),
				user.getEmail(), user.getPassword());
		return new ResponseEntity<>(newUser.toDTO(), HttpStatus.OK);
	}

	@PostMapping(path = "/confirmemail")
	public ResponseEntity<AppUserDTO> confirmEmail(@RequestBody String id)
			throws ExpiredLinkException, UserNotFoundException {
		logEmailAction("initiating email confirmation process");
		AppUser newUser = userService.confirmEmail(id);
		logEmailAction("email confirmation process completed");
		return new ResponseEntity<>(newUser.toDTO(), HttpStatus.OK);
	}

	/*
	 * @PostMapping(path = "/setrole") public ResponseEntity<AppUserDTO>
	 * setRole(@RequestBody AppUserDTO user) throws UserNotFoundException,
	 * UsernameExistsException, EmailExistsException, AddressException,
	 * MessagingException, UserFieldsNotValidException {
	 * userService.setRole(user.getUserName()); return new ResponseEntity<>(user,
	 * HttpStatus.OK); }
	 */
	/**
	 * 
	 * @param user
	 * @return
	 * @throws UserNotFoundException
	 * @throws UsernameExistsException
	 * @throws EmailExistsException
	 * @throws ParseException 
	 */
	@PostMapping(path = "/login")
	public ResponseEntity<AppUserDTO> login(@RequestBody AppUserDTO user)
			throws UserNotFoundException, UsernameExistsException, EmailExistsException, ParseException {
		logAction("initiating the login process, authenticating the user");
		authenticate(user.getUserName(), user.getPassword());
		logAction("user has been authenticated");
		AppUserDTO loggedInUser = userService.findUserByUsername(user.getUserName()).toDTO();
		userService.checkPasswordResetTable(loggedInUser);
		UserPrincipal loggedInUserPrincipal = new UserPrincipal(loggedInUser);
		HttpHeaders jwtHeader = getJwtHeader(loggedInUserPrincipal);
		logAction("login process completed");
		return new ResponseEntity<>(loggedInUser, jwtHeader, HttpStatus.OK);
	}

	/**
	 * the 3 methods below are all used as part of the password reset functionality 
	 */
	
	/**
	 * this method is for people who have forgotten their password; it will send
	 * them an email with a new password in it
	 * @param email
	 * @return
	 * @throws MessagingException
	 * @throws EmailNotFoundException
	 */
	@PostMapping("/forgotpassword")
	public ResponseEntity<HttpResponse> forgotPassword(@RequestBody String email)
			throws MessagingException, EmailNotFoundException {
		logEmailAction("initiating the forgot password reset process");
		userService.resetPasswordInitiation(email);
		logEmailAction("forgot password reset process completed");
		return response(HttpStatus.OK, EMAIL_SENT + email);
	}

	/**
	 * this method is called upon initial load of the password reset page link that the user clicks in their email.
	 * it checks to make sure that:
	 * 1. there is an actual record in the password_rest table
	 * 2. the record is not expired
	 * @param code
	 * @return
	 * @throws ExpiredLinkException
	 * @throws UserNotFoundException
	 * @throws AddressException
	 * @throws MessagingException
	 */
	@PostMapping(path = "/checkreset")
	public ResponseEntity<CheckPasswordResetResponseDTO> checkPasswordReset(@RequestBody String code)
			throws ExpiredLinkException, UserNotFoundException, AddressException, NoSuchProviderException,
			SendFailedException, MessagingException {
		logEmailAction("initiating the check password reset process");
		CheckPasswordResetResponseDTO results = userService.checkPasswordResetTable(code);
		logEmailAction("check password reset process completed");
		return new ResponseEntity<>(results, HttpStatus.OK);
	}
	
	/**
	 * this method is called once the user actually resets their password on the page link that the user clicks in their email.
	 * @param input
	 * @return
	 * @throws ExpiredLinkException
	 * @throws UserNotFoundException
	 * @throws AddressException
	 * @throws NoSuchProviderException
	 * @throws SendFailedException
	 * @throws MessagingException
	 * @throws PasswordResetException
	 */
	@PostMapping(path = "/finalizepassword")
	public ResponseEntity<AppUserDTO> finalizePasswordReset(@RequestBody PasswordResetDTO input)
			throws ExpiredLinkException, UserNotFoundException, AddressException, NoSuchProviderException, SendFailedException, MessagingException, PasswordResetException {
		logEmailAction("initiating the password reset finalization process");
		AppUserDTO results = userService.changePassword(input);
		logEmailAction("password reset finalization process completed");
		return new ResponseEntity<>(results, HttpStatus.OK);
	}
	
	/**
	 * this method is for people who have forgotten their username; it will send
	 * them an email with their username in it
	 * @param email
	 * @return
	 * @throws MessagingException
	 * @throws EmailNotFoundException
	 */
	@PostMapping("/forgotusername")
	public ResponseEntity<HttpResponse> forgotUsername(@RequestBody String email)
			throws MessagingException, EmailNotFoundException {
		logEmailAction("initiating the forgot username email process");
		userService.forgotUsername(email);
		logEmailAction("forgot username email process completed");
		return response(HttpStatus.OK, EMAIL_SENT + email);
	}

	/**
	 * update an AppUser by the same user
	 * update a user's contact info
	 * @param data
	 * @return AppUser
	 */
	@PostMapping(path = "/updateuserbyuser")
	@PreAuthorize("hasAnyAuthority('self:update')")
	public ResponseEntity<AppUserDTO> updateUserByUser(@Valid @RequestBody AppUserDTOWrapper data)
			throws Exception {
		if (data.getNewPassword() != null) {
			try {
				authenticate(data.getUserName(), data.getPassword());
			} catch (Exception e) {
				throw new PasswordIncorrectException(INCORRECT_OLD_PASSWORD);
			}
		}
		AppUserDTO updatedData = userService.updateUserByUser(data).toDTO();
		return ResponseEntity.status(HttpStatus.OK).body(updatedData);
	}

	/**
	 * get all AppUser records
	 * 
	 * @return Iterable<AppUser>
	 */
	@GetMapping(path = "/all")
	@PreAuthorize("hasAnyAuthority('admin:select')")
	public ResponseEntity<List<AppUserDTO>> getAllAppUser() throws Exception {
		List<AppUserDTO> interviewuser = userBusinessService.getAllAppUser();
		return ResponseEntity.status(HttpStatus.OK).body(interviewuser);
	}

	/**
	 * get AppUser by primary key
	 * 
	 * @param id
	 * @return AppUser
	 */
	@GetMapping(path = "/findById/{id}")
	@PreAuthorize("hasAnyAuthority('admin:select')")
	public ResponseEntity<AppUserDTO> findAppUserById(@PathVariable int id) throws Exception {
		AppUserDTO results = userBusinessService.findAppUserById(id);
		return ResponseEntity.status(HttpStatus.OK).body(results);
	}

	/**
	 * create a new AppUser
	 * 
	 * @param data
	 * @return AppUser
	 */
	@PostMapping(path = "/create")
	@PreAuthorize("hasAnyAuthority('admin:create','user:create')")
	public ResponseEntity<AppUserDTO> createAppUser(@RequestHeader("Authorization") String token,
			@Valid @RequestBody AppUserDTO data) throws Exception {
		String alteredToken = removeBearerFromToken(token);
		String adminUser = jwtTokenProvider.getSubject(alteredToken);
		String[] authorizations = jwtTokenProvider.getClaimsFromToken(alteredToken);
		AppUserDTO createdData = userBusinessService.createAppUser(data, authorizations, adminUser);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdData);

	}

	/**
	 * update a AppUser note: we cannot simply update via the primary key, as
	 * we never pass this value back out to the front end so we have to update by
	 * username, and account for if they are changing the username
	 * 
	 * @param data
	 * @return AppUser
	 */
	@PostMapping(path = "/update")
	@PreAuthorize("hasAnyAuthority('admin:update','user:update')")
	public ResponseEntity<AppUserDTO> updateAppUser(@RequestHeader("Authorization") String token,
			@Valid @RequestBody AppUserDTOWrapper data) throws Exception {
		String alteredToken = removeBearerFromToken(token);
		String adminUser = jwtTokenProvider.getSubject(alteredToken);
		String[] authorizations = jwtTokenProvider.getClaimsFromToken(alteredToken);
		AppUserDTO updatedData = userBusinessService.updateAppUser(data, authorizations, adminUser);
		return ResponseEntity.status(HttpStatus.OK).body(updatedData);
	}

	/**
	 * delete a AppUser by their username
	 * 
	 * @param username
	 */
	@DeleteMapping(path = "/delete/{username}")
	@PreAuthorize("hasAnyAuthority('admin:delete','user:delete')")
	public ResponseEntity<ResponseMessage> deleteAppUser(@RequestHeader("Authorization") String token,
														 @PathVariable String username) throws Exception {
		String alteredToken = removeBearerFromToken(token);
		String[] authorizations = jwtTokenProvider.getClaimsFromToken(alteredToken);
		String adminUser = jwtTokenProvider.getSubject(alteredToken);
		userBusinessService.deleteAppUser(username, authorizations, adminUser);
		ResponseMessage rb = new ResponseMessage("successfully deleted");
		return ResponseEntity.status(HttpStatus.OK).body(rb);
	}

	/**
	 * 
	 * @param token
	 * @return
	 */
	private String removeBearerFromToken(String token) {
		return token.replace(Constants.TOKEN_PREFIX, "");
	}

	/**
	 * method to call the AuthenticationManager to authenticate the user's
	 * username/password against what is stored in the database
	 * 
	 * @param userName
	 * @param password
	 */
	private void authenticate(String userName, String password) {
		this.authManager.authenticate(new UsernamePasswordAuthenticationToken(userName, password));
	}

	/**
	 * method that makes an HttpHeaders object, generates the Jwt Token, and adds it
	 * to the headers
	 * 
	 * @param loggedInUserPrincipal
	 * @return
	 * @throws ParseException 
	 */
	private HttpHeaders getJwtHeader(UserPrincipal loggedInUserPrincipal) throws ParseException {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Access-Control-Expose-Headers", JWT_TOKEN_HEADER);
		headers.add(Constants.JWT_TOKEN_HEADER, this.jwtTokenProvider.generateJwtToken(loggedInUserPrincipal));
		return headers;
	}

	/**
	 * method for creating an HttpResponse netity
	 * 
	 * @param httpStatus
	 * @param message
	 * @return
	 */
	private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
		return new ResponseEntity<>(
				new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
				httpStatus);
	}

	@PostMapping(path = "/setrole")
	public ResponseEntity<AppUserDTO> setRole(@RequestBody AppUserDTO user)
			throws UserNotFoundException, UsernameExistsException, EmailExistsException, AddressException,
			MessagingException, UserFieldsNotValidException {
		userService.setRole(user.getUserName());
		return new ResponseEntity<>(user, HttpStatus.OK);
	}

	/**
	 * 
	 * @param user
	 * @return
	 * @throws UserNotFoundException
	 * @throws UsernameExistsException
	 * @throws EmailExistsException
	 * @throws AddressException
	 * @throws MessagingException
	 * @throws UserFieldsNotValidException
	 */
	@PostMapping(path = "/setpwd")
	public ResponseEntity<AppUserDTO> setPassword(@RequestBody AppUserDTO user)
			throws UserNotFoundException, UsernameExistsException, EmailExistsException, AddressException,
			MessagingException, UserFieldsNotValidException {
		userService.setPassword(user.getUserName(), user.getPassword());
		return new ResponseEntity<>(user, HttpStatus.OK);
	}

	@GetMapping("/testemail")
	public ResponseEntity<HttpResponse> testEmail() throws AddressException, MessagingException, Exception {
		logEmailAction("running an email test");
		userService.testEmail();
		logEmailAction("email test complete");
		return response(HttpStatus.OK, EMAIL_SENT);
	}
	
	/**
	 * logging method
	 * 
	 * @param message
	 */
	private static void logAction(String message) {
		System.out.println("AppUserController: " + message);
		applicationLogger.debug("AppUserController: " + message);
	}
	
	/**
	 * logging method
	 * 
	 * @param message
	 */
	private static void logEmailAction(String message) {
		System.out.println("AppUserController: " + message);
		emailLogger.debug("AppUserController: " + message);
	}

}
