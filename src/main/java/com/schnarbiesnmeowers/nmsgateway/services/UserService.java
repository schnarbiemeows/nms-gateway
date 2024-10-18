package com.schnarbiesnmeowers.nmsgateway.services;

import com.schnarbiesnmeowers.nmsgateway.dtos.CheckPasswordResetResponseDTO;
import com.schnarbiesnmeowers.nmsgateway.dtos.AppUserDTO;
import com.schnarbiesnmeowers.nmsgateway.dtos.AppUserDTOWrapper;
import com.schnarbiesnmeowers.nmsgateway.dtos.PasswordResetDTO;
import com.schnarbiesnmeowers.nmsgateway.exceptions.user.*;
import com.schnarbiesnmeowers.nmsgateway.entities.AppUser;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
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

	Mono<AppUser> register(String firstName, String lastName, String username, String email, String password) throws UserNotFoundException, UsernameExistsException, EmailExistsException, AddressException, MessagingException;
	public Mono<Void> setPassword(String username, String password);
	public Mono<Void> setRole(String username);
	Flux<AppUser> getAllUsers();
	Mono<AppUser> findUserByUsername(String username);
	Flux<AppUser> getUsersByRole(String role);
	Flux<AppUser> getJustUsers();
	Flux<AppUser> getAdmins();
	Mono<AppUser> findUserByEmail(String email);
	public String encodePassword(String password);
	public String generateUserIdentifier();
	public Mono<AppUser> validateNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail) throws UserNotFoundException, UsernameExistsException, EmailExistsException ;
	Mono<AppUser> addNewUser(String firstName, String lastName, String username, String email, String role, boolean isNotLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageFileException;
	//Mono<AppUser> updateUser(String currentUserName, String firstName, String lastName, String username, String email, String role, boolean isNotLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageFileException;
	Mono<AppUser> updateUserByUser(AppUserDTOWrapper user) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, PasswordIncorrectException;
	Mono<Void> deleteUser(String username) throws IOException;
	Mono<Void> resetPasswordInitiation(String email) throws AddressException, MessagingException, EmailNotFoundException;
	Mono<Void> forgotUsername(String email) throws AddressException, MessagingException, EmailNotFoundException;
	Mono<AppUser> updateProfileImage(String username, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageFileException;
	public String getTemporaryImageUrl(String username);
	public Mono<Void> testEmail() throws AddressException, MessagingException, Exception;
	Mono<AppUser> confirmEmail(String id) throws ExpiredLinkException, UserNotFoundException;
	CheckPasswordResetResponseDTO checkPasswordResetTable(String id) throws AddressException,
			NoSuchProviderException, SendFailedException, MessagingException;
	AppUserDTO changePassword(PasswordResetDTO input) throws AddressException, NoSuchProviderException,
			SendFailedException, MessagingException, PasswordResetException;
	Mono<Void> checkThePasswordResetTable(String email);
}
