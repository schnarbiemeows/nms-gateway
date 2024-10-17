package com.schnarbiesnmeowers.nmsgateway.services;

import com.schnarbiesnmeowers.nmsgateway.dtos.CheckPasswordResetResponseDTO;
import com.schnarbiesnmeowers.nmsgateway.dtos.AppUserDTO;
import com.schnarbiesnmeowers.nmsgateway.dtos.AppUserDTOWrapper;
import com.schnarbiesnmeowers.nmsgateway.dtos.PasswordResetDTO;
import com.schnarbiesnmeowers.nmsgateway.exceptions.user.*;
import com.schnarbiesnmeowers.nmsgateway.entities.AppUser;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.internet.*;
import javax.mail.*;

/**
 * 
 * @author Dylan I. Kessler
 *
 */
public interface UserService {

	AppUser register(String firstName, String lastName, String username, String email, String password) throws UserNotFoundException, UsernameExistsException, EmailExistsException, AddressException, MessagingException;
	public void setPassword(String username, String password);
	public void setRole(String username);
	List<AppUser> getAllUsers();
	AppUser findUserByUsername(String username);
	List<AppUser> getUsersByRole(String role);
	List<AppUser> getJustUsers();
	List<AppUser> getAdmins();
	AppUser findUserByEmail(String email);
	public String encodePassword(String password);
	public String generateUserIdentifier();
	public AppUser validateNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail) throws UserNotFoundException, UsernameExistsException, EmailExistsException ;
	AppUser addNewUser(String firstName, String lastName, String username, String email, String role, boolean isNotLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageFileException;
	//AppUser updateUser(String currentUserName, String firstName, String lastName, String username, String email, String role, boolean isNotLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageFileException;
	AppUser updateUserByUser(AppUserDTOWrapper user) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, PasswordIncorrectException;
	void deleteUser(String username) throws IOException;
	void resetPasswordInitiation(String email) throws AddressException, MessagingException, EmailNotFoundException;
	void forgotUsername(String email) throws AddressException, MessagingException, EmailNotFoundException;
	AppUser updateProfileImage(String username, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageFileException;
	public String getTemporaryImageUrl(String username);
	public void testEmail() throws AddressException, MessagingException, Exception;
	AppUser confirmEmail(String id) throws ExpiredLinkException, UserNotFoundException;
	CheckPasswordResetResponseDTO checkPasswordResetTable(String id) throws AddressException,
			NoSuchProviderException, SendFailedException, MessagingException;
	AppUserDTO changePassword(PasswordResetDTO input) throws AddressException, NoSuchProviderException,
			SendFailedException, MessagingException, PasswordResetException;
	void checkPasswordResetTable(AppUserDTO loggedInUser);
}
